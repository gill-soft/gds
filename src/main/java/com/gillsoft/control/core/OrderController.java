package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.NoDataFoundException;
import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.PrintTicketService;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.control.service.model.PrintOrderWrapper;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.Document;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.PaymentMethod;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.TicketLayout;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderController {
	
	private static Logger LOGGER = LogManager.getLogger(OrderController.class);
	
	@Autowired
	private AgregatorOrderService service;
	
	@Autowired
	private PrintTicketService printService;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private TripSearchController searchController;
	
	@Autowired
	private OrderRequestValidator validator;
	
	@Autowired
	private OrderResponseConverter converter;
	
	@Autowired
	private OrderDAOManager manager;
	
	@Autowired
	private OperationLocker locker;
	
	public OrderResponse create(OrderRequest request) {
			
		// заказ для сохранения
		return saveOrder(createInResource(request));
	}
	
	private Order createInResource(OrderRequest request) {
		
		// проверяем параметры запроса
		validator.validateOrderRequest(request);
		
		// валидируем обязательные поля для оформления
		validator.validateRequiredFields(request);
		
		// получаем все рейсы, чтобы вернуть потом в заказе
		OrderResponse result = searchController.search(request);
		
		// создаем заказ в ресурсе
		OrderRequest createRequest = createRequest(request);
		OrderResponse response = service.create(createRequest);
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		}
		if (response.getResources() != null
				&& !response.getResources().isEmpty()) {
			
			// заказ для сохранения
			return converter.convertToNewOrder(request, createRequest, result, response);
		} else {
			throw new ApiException("Empty response");
		}
	}
	
	private OrderRequest createRequest(OrderRequest request) {
		List<Resource> resources = getResources();
		Map<Long, OrderRequest> requests = new HashMap<>();
		for (ServiceItem item : request.getServices()) {
			ServiceItem resourceItem = (ServiceItem) SerializationUtils.deserialize(SerializationUtils.serialize(item));
			TripIdModel idModel = new TripIdModel().create(resourceItem.getSegment().getId());
			
			// проверяем ресурс
			Resource serviceResource = getResource(idModel.getResourceId(), resources);
			if (serviceResource == null) {
				throw new ResourceUnavailableException("Resource is unavailable for service where segmentId="
						+ resourceItem.getSegment().getId());
			}
			// проверяем доступность метода
			if (!infoController.isMethodAvailable(serviceResource, Method.ORDER, MethodType.POST)) {
				throw new MethodUnavalaibleException("Method is unavailable for service where segmentId="
						+ resourceItem.getSegment().getId());
			}
			OrderRequest resourceRequest = requests.get(serviceResource.getId());
			if (resourceRequest == null) {
				resourceRequest = new OrderRequest();
				resourceRequest.setId(StringUtil.generateUUID());
				resourceRequest.setLang(request.getLang());
				resourceRequest.setCurrency(request.getCurrency());
				resourceRequest.setParams(serviceResource.createParams());
				resourceRequest.setServices(new ArrayList<>());
				requests.put(serviceResource.getId(), resourceRequest);
			}
			resourceItem.getSegment().setId(idModel.getId());
			if (resourceItem.getCarriage() != null) {
				resourceItem.getCarriage().setId(new TripIdModel().create(resourceItem.getCarriage().getId()).getId());
			}
			if (resourceItem.getAdditionals() == null) {
				resourceItem.setAdditionals(new HashMap<>());
			}
			try {
				resourceItem.getAdditionals().put("uniqueId", String.valueOf(manager.getUniqueId(serviceResource.getId())));
			} catch (ManageException e) {
				LOGGER.error("Can not create unique id for resource " + serviceResource.getId(), e);
			}
			resourceRequest.getServices().add(resourceItem);
		}
		OrderRequest newRequest = new OrderRequest();
		newRequest.setCustomers(request.getCustomers());
		newRequest.setResources(new ArrayList<>(requests.values()));
		return newRequest;
	}
	
	private List<OrderRequest> operationRequests(Order order, String method, Set<ServiceStatus> statuses) {
		List<Resource> resources = getResources();
		List<OrderRequest> requests = new ArrayList<>();
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (statuses == null
					|| isStatus(statuses, resourceOrder)) {
			
				// проверяем ресурс
				Resource serviceResource = getResource(resourceOrder.getResourceId(), resources);
				if (serviceResource == null) {
					throw new ResourceUnavailableException("Resource is unavailable for service ids "
							+ Arrays.toString(resourceOrder.getServices().stream().map(ResourceService::getId)
									.collect(Collectors.toList()).toArray()));
				}
				// проверяем доступность метода
				if (!infoController.isMethodAvailable(serviceResource, method, MethodType.POST)) {
					throw new MethodUnavalaibleException("Method is unavailable for service ids "
							+ Arrays.toString(resourceOrder.getServices().stream().map(ResourceService::getId)
									.collect(Collectors.toList()).toArray()));
				}
				OrderRequest resourceRequest = new OrderRequest();
				resourceRequest.setId(StringUtil.generateUUID());
				resourceRequest.setParams(serviceResource.createParams());
				resourceRequest.setOrderId(new IdModel().create(resourceOrder.getResourceNativeOrderId()).getId());
				requests.add(resourceRequest);
			}
		}
		return requests;
	}
	
	private Set<ServiceStatus> getStatusesForBooking() {
		Set<ServiceStatus> statuses = new HashSet<>();
		statuses.add(ServiceStatus.NEW);
		statuses.add(ServiceStatus.BOOKING_ERROR);
		return statuses;
	}
	
	private Set<ServiceStatus> getStatusesForConfirm() {
		Set<ServiceStatus> statuses = new HashSet<>();
		statuses.add(ServiceStatus.NEW);
		statuses.add(ServiceStatus.BOOKING);
		statuses.add(ServiceStatus.BOOKING_ERROR);
		statuses.add(ServiceStatus.CONFIRM_ERROR);
		return statuses;
	}
	
	private Set<ServiceStatus> getStatusesForReturn() {
		Set<ServiceStatus> statuses = new HashSet<>();
		statuses.add(ServiceStatus.CONFIRM);
		statuses.add(ServiceStatus.RETURN_ERROR);
		return statuses;
	}
	
	private Set<ServiceStatus> getStatusesForCancel() {
		Set<ServiceStatus> statuses = new HashSet<>();
		statuses.add(ServiceStatus.NEW);
		statuses.add(ServiceStatus.NEW_ERROR);
		statuses.add(ServiceStatus.CONFIRM);
		statuses.add(ServiceStatus.CONFIRM_ERROR);
		statuses.add(ServiceStatus.BOOKING);
		statuses.add(ServiceStatus.BOOKING_ERROR);
		statuses.add(ServiceStatus.CANCEL_ERROR);
		return statuses;
	}
	
	private void checkStatus(Order order, Set<ServiceStatus> statuses) {
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (isStatus(statuses, resourceOrder)) {
				return;
			}
		}
		throw new MethodUnavalaibleException("Order status is not one of "
				+ String.join(", ", statuses.stream().map(s -> s.name()).collect(Collectors.toSet())));
	}
	
	private boolean isStatus(Set<ServiceStatus> statuses, ResourceOrder resourceOrder) {
		
		// если последний статус хоть одной продажи находится в списке перечисленных статусов 
		return resourceOrder.getServices().stream().anyMatch(s -> statuses.contains(converter.getLastStatus(s.getStatuses())));
	}
	
	private List<Resource> getResources() {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			return resources;
		}
		throw new ResourceUnavailableException("User does not has available resources");
	}
	
	private Resource getResource(long resourceId, List<Resource> resources) {
		for (Resource resource : resources) {
			if (resourceId == resource.getId()) {
				return resource;
			}
		}
		return null;
	}
	
	/*
	 * Сохраняем и устанавливаем все необходимые поля.
	 */
	private OrderResponse saveOrder(Order order) {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			try {
				return converter.getResponse(manager.create(order));
			} catch (ManageException e) {
				LOGGER.error(e);
				throw new ApiException(e);
			}
		}
		throw new ResourceUnavailableException("User does not has available resources");
	}
	
	public OrderResponse booking(long orderId) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
		
			Order order = findOrder(orderId, ServiceStatus.BOOKING);
			
			// проверяем статус заказа. выкупить можно NEW, RESERV_ERROR
			Set<ServiceStatus> statuses = getStatusesForBooking();
			checkStatus(order, statuses);
			
			List<OrderRequest> requests = operationRequests(order, Method.ORDER_BOOKING, statuses);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// подтверждаем в ресурсах
			List<OrderResponse> responses = service.booking(requests);
			
			// преобразовываем и сохраняем
			order = converter.convertToConfirm(order, requests, responses, ServiceStatus.BOOKING, ServiceStatus.BOOKING_ERROR);
			try {
				manager.booking(order);
			} catch (ManageException e) {
				LOGGER.error("Booking order error in db", e);
			}
			return converter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	public OrderResponse confirm(long orderId, PaymentMethod paymentMethod) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
			
			Order order = findOrder(orderId, ServiceStatus.CONFIRM);
			
			// проверяем статус заказа. выкупить можно NEW, RESERV, RESERV_ERROR, CONFIRM_ERROR
			Set<ServiceStatus> statuses = getStatusesForConfirm();
			checkStatus(order, statuses);
			
			List<OrderRequest> requests = operationRequests(order, Method.ORDER_CONFIRM, statuses);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// подтверждаем в ресурсах
			List<OrderResponse> responses = service.confirm(requests);
			
			// преобразовываем и сохраняем
			order = converter.convertToConfirm(order, requests, responses, ServiceStatus.CONFIRM, ServiceStatus.CONFIRM_ERROR);
			if (paymentMethod != null) {
				order.setPayment(paymentMethod);
			}
			try {
				manager.confirm(order);
			} catch (ManageException e) {
				LOGGER.error("Confirm order error in db", e);
			}
			return converter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	public OrderResponse cancel(long orderId, String reason) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
		
			Order order = findOrder(orderId, ServiceStatus.CANCEL);

			// проверяем статус заказа. аннулировать можно NEW, CONFIRM_ERROR, RESERVE, RESERVE_ERROR, CONFIRM, CANCEL_ERROR
			Set<ServiceStatus> statuses = getStatusesForCancel();
			checkStatus(order, statuses);
			
			List<OrderRequest> requests = operationRequests(order, Method.ORDER_CANCEL, statuses);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// подтверждаем в ресурсах
			List<OrderResponse> responses = service.cancel(requests);
			
			// преобразовываем и сохраняем
			order = converter.convertToConfirm(order, requests, responses, ServiceStatus.CANCEL, ServiceStatus.CANCEL_ERROR);
			order.setCancelReason(reason);
			try {
				manager.cancel(order);
			} catch (ManageException e) {
				LOGGER.error("Cancel order error in db", e);
			}
			return converter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	public OrderResponse getOrder(long orderId) {
		Order order = findOrder(orderId, ServiceStatus.VIEW, "Order is unavailable");
		return converter.getResponse(order);
	}
	
	public OrderResponse getService(long serviceId) {
		OrderParams params = new OrderParams();
		params.setServiceId(serviceId);
		Order order = findOrder(params);
		if (!dataController.isOrderAvailable(order, ServiceStatus.VIEW)) {
			throw new NoDataFoundException("Order is unavailable");
		}
		return converter.getService(converter.getResponse(order), serviceId);
	}
	
	private Order findOrder(long orderId, ServiceStatus status) {
		return findOrder(orderId, status, "Operation is unavailable on order for this user");
	}
	
	private Order findOrder(long orderId, ServiceStatus status, String unavailableMessage) {
		Order order = findOrder(orderId);
		if (!dataController.isOrderAvailable(order, status)) {
			throw new NoDataFoundException(unavailableMessage);
		}
		return order;
	}
	
	private Order findOrder(long orderId) {
		OrderParams params = new OrderParams();
		params.setOrderId(orderId);
		return findOrder(params);
	}
	
	private Order findOrder(OrderParams params) {
		Order order = null;
		try {
			order = manager.get(params);
		} catch (ManageException e) {
			LOGGER.error("Find order error in db", e);
		}
		if (order == null) {
			throw new NoDataFoundException("Order not found");
		}
		return order;
	}
	
	public OrderResponse addService(long orderId, OrderRequest request) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
		
			Order order = findOrder(orderId, ServiceStatus.NEW);
			
			// проверяем статус заказа. добавить в заказ можно, если он в статусе NEW, RESERV, RESERV_ERROR, CONFIRM_ERROR
			checkStatus(order, getStatusesForConfirm());
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// создаем заказ в ресурсах, объединяем с имеющимся и сохраняем 
			Order newOrder = createInResource(request);
			order = converter.joinOrders(order, newOrder);
			try {
				order = manager.addServices(order);
			} catch (ManageException e) {
				LOGGER.error("Add services to order error in db", e);
			}
			return converter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	public OrderResponse removeService(long orderId, OrderRequest request) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
		
			Order order = findOrder(orderId, ServiceStatus.NEW);
	
			// проверяем статус заказа. удалить из заказа можно, если он в статусе NEW, RESERV, RESERV_ERROR, CONFIRM_ERROR
			checkStatus(order, getStatusesForConfirm());
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// создаем заказ в ресурсах, объединяем с имеющимся и сохраняем 
			order = converter.removeServices(order, request.getServices());
			try {
				order = manager.removeServices(order);
			} catch (ManageException e) {
				LOGGER.error("Remove services from order error in db", e);
			}
			return converter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	private Order getOrderDocuments(long orderId) {
		OrderParams params = new OrderParams();
		params.setOrderId(orderId);
		return getOrderDocuments(params);
	}
	
	private Order getOrderDocuments(OrderParams params) {
		Order order = null;
		try {
			order = manager.getDocuments(params);
		} catch (ManageException e) {
			LOGGER.error("Find order documents error in db", e);
		}
		return order;
	}
	
	public OrderResponse getDocuments(long orderId) {
		
		// проверяем билеты в заказе
		Order order = getOrderDocuments(orderId);
		if (!dataController.isOrderAvailable(order, ServiceStatus.VIEW)) {
			throw new NoDataFoundException("Order is unavailable");
		}
		if (order.getDocuments() != null
				&& !order.getDocuments().isEmpty()) {
			addSystemDocuments(order);
			return converter.getDocumentsResponse(order);
		}
		try {
			// если нет, то берем у ресурсов и сохраняем что есть
			List<OrderRequest> requests = operationRequests(order, Method.ORDER_DOCUMENTS, null);
			
			// получаем документы в ресурсах
			List<OrderResponse> responses = service.getPdfDocuments(requests);
			
			// сохраняем документы по заказу
			order = converter.addDocuments(order, requests, responses);
			try {
				order = manager.update(order);
			} catch (ManageException e) {
				LOGGER.error("Save documents to order error in db", e);
			}
		} catch (Exception e) {
			LOGGER.error("Can not get documents from resource.", e);
		}
		addSystemDocuments(order);
		return converter.getDocumentsResponse(order);
	}
	
	private void addSystemDocuments(Order order) {
		
		// макеты по ресурсам заказа и текущему пользователю
		Map<Long, List<TicketLayout>> layouts = dataController.getTicketLayouts(order);
		
		// конвертируем заказ в ответ
		OrderResponse response = converter.getResponse(order);
		
		// проставляем данные со словарей
		converter.updateSegments(response);
		List<ServiceItem> items = response.getServices();
		
		// формируем билет по каждой позиции отдельно
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (layouts.containsKey(resourceOrder.getResourceId())) {
				List<TicketLayout> ticketLayouts = layouts.get(resourceOrder.getResourceId());
				
				// сервисы, по которым уже создан билет
				Set<ResourceService> processedServices = new HashSet<>();
				for (ResourceService service : resourceOrder.getServices()) {
					if (!processedServices.contains(service)) {
						
						// берем статус, по которому формируется документ
						ServiceStatusEntity status = converter.getLastNotErrorStatusEntity(service.getStatuses());
						if (status.getError() == null) {
							
							// берем макет соответствующий последнему статусу
							if (ticketLayouts != null) {
								Optional<TicketLayout> layout = ticketLayouts.stream().filter(l -> l.getServiceStatus() == status.getStatus()).findFirst();
								if (layout.isPresent()) {
									TicketLayout ticketLayout = layout.get();
									if (ticketLayout.getLayout() != null
											&& !ticketLayout.getLayout().isEmpty()) {
									
										// TODO может стоит брать юзера со статуса ???
										
										ServiceItem item = null;
										ServiceStatus orderStatus = null;
										List<ServiceItem> services = new ArrayList<>();
										switch (ticketLayout.getApplyingArea()) {
										
										// если печать для всего заказа, то выбираем все сервисы заказа ресурса со статусом текущей позиции
										case RESOURCE_ORDER:
											orderStatus = status.getStatus();
											for (ResourceService otherService : resourceOrder.getServices()) {
												ServiceStatusEntity otherStatus = converter.getLastNotErrorStatusEntity(otherService.getStatuses());
												if (otherStatus.getError() == null
														&& status.getStatus() == otherStatus.getStatus()) {
													processedServices.add(otherService);
													services.add(items.stream().filter(s -> Long.valueOf(s.getId()) == otherService.getId()).findFirst().get());
												}
											}
											break;
										case SINGLE_SERVICE:
											processedServices.add(service);
											item = items.stream().filter(s -> Long.valueOf(s.getId()) == service.getId()).findFirst().get();
											services.add(item);
											break;
										default:
											break;
										}
										response.setServices(services);
										PrintOrderWrapper orderWrapper = new PrintOrderWrapper();
										orderWrapper.setOrder(response);
										orderWrapper.setTicketLayout(layout.get().getLayout());
										List<Document> documents = printService.create(orderWrapper);
										converter.addDocuments(order, orderStatus, documents, item);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public OrderResponse calcReturn(long orderId, OrderRequest request) {
		Order order = findOrder(orderId, ServiceStatus.RETURN);
		
		// проверяем статус заказа. вернуть можно CONFIRM, RETURN_ERROR
		checkStatus(order, getStatusesForReturn());
		List<OrderRequest> requests = returnRequests(order, request, Method.ORDER_RETURN_PREPARE);
		
		List<OrderResponse> responses = service.prepareReturnServices(requests);
		
		return converter.convertToReturnCalc(order, requests, responses);
	}
	
	public OrderResponse confirmReturn(long orderId, OrderRequest request) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
		
			Order order = findOrder(orderId, ServiceStatus.RETURN);
			
			// проверяем статус заказа. вернуть можно CONFIRM, RETURN_ERROR
			checkStatus(order, getStatusesForReturn());
			List<OrderRequest> requests = returnRequests(order, request, Method.ORDER_RETURN_CONFIRM);
			
			// получаем стоимости возврата на случай, если они не будут возвращены в самом возврате
			List<OrderResponse> calcResponses = service.prepareReturnServices(requests);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
						
			// возвращаем сервисы
			List<OrderResponse> returnResponses = service.returnServices(requests);
			order = converter.convertToReturn(order, requests, returnResponses, calcResponses);
			try {
				manager.returnServices(order);
			} catch (ManageException e) {
				LOGGER.error("Return services error in db", e);
			}
			return converter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	private List<OrderRequest> returnRequests(Order order, OrderRequest request, String method) {
		Set<ServiceStatus> statuses = getStatusesForReturn();
		List<OrderRequest> requests = operationRequests(order, method, statuses);
		
		// добавляем ид сервисов к возврату
		// перебираем сформированные запросы и сравниваем с имеющимися заказами в ресурсе
		// потом перебираем сервисы ресурсов и сравниваем с сервисами в запросе
		// те заказы, сервисов которых нет, удаляем с запроса
		// у заказов, которые есть, проверяем сервисы по статусу и добавляем к запросу
		StringBuilder errorMsg = new StringBuilder();
		for (Iterator<OrderRequest> iterator = requests.iterator(); iterator.hasNext();) {
			OrderRequest orderRequest = iterator.next();
			for (ResourceOrder resourceOrder : order.getOrders()) {
				if (Objects.equals(orderRequest.getOrderId(), new IdModel().create(resourceOrder.getResourceNativeOrderId()).getId())) {
					for (ServiceItem service : request.getServices()) {
						for (ResourceService resourceService : resourceOrder.getServices()) {
							if (Objects.equals(service.getId(), String.valueOf(resourceService.getId()))) {
								if (statuses.contains(converter.getLastStatus(resourceService.getStatuses()))) {
									
									// устанавливаем ид ресурса и добавляем в запрос
									service.setId(new IdModel().create(resourceService.getResourceNativeServiceId()).getId());
									if (orderRequest.getServices() == null) {
										orderRequest.setServices(new ArrayList<>());
									}
									orderRequest.getServices().add(service);
								} else {
									errorMsg.append("Service id=").append(service.getId()).append(" status is not ").append(ServiceStatus.CONFIRM).append("\r\n");
								}
								break;
							}
						}
					}
					break;
				}
			}
			if (orderRequest.getServices() == null) {
				iterator.remove();
			}
		}
		if (errorMsg.length() > 0) {
			throw new MethodUnavalaibleException(errorMsg.toString().trim());
		}
		return requests;
	}
	
	public List<Order> getOrders(int count) {
		
		// возвращать не больше 1000 заказов
		if (count > 1000) {
			throw new RequestValidateException("To many rows by request.");
		}
		OrderParams params = new OrderParams();
		params.setCount(count);
		try {
			List<Order> orders = converter.addPrice(manager.getOrders(params));
			
			// удаляем уже переданные статусы
			orders.forEach(
					o -> o.getOrders().forEach(
							ro -> ro.getServices().forEach(
									rs -> rs.getStatuses().removeIf(ServiceStatusEntity::isReported))));
			return orders;
		} catch (ManageException e) {
			LOGGER.error("Get orders error in db", e);
			throw new ApiException(e);
		}
	}
	
	public void reportStatuses(Set<Long> ids) {
		try {
			manager.reportStatuses(ids);
		} catch (ManageException e) {
			LOGGER.error("Report statuses error in db", e);
			throw new ApiException(e);
		}
	}
	
}
