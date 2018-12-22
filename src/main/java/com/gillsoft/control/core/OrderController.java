package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.NoDataFoundException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.Status;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderController {
	
	private static Logger LOGGER = LogManager.getLogger(OrderController.class);
	
	@Autowired
	private AgregatorOrderService service;
	
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
	
	public OrderResponse create(OrderRequest request) {
		
		// проверяем параметры запроса
			
		// заказ для сохранения
		return saveOrder(createInResource(request));
	}
	
	private Order createInResource(OrderRequest request) {
		
		// проверяем параметры запроса
		validator.validateOrderRequest(request);
		
		// валидируем обязательные поля для оформления
		validator.validateRequiredFields(request);
		
		// получаем все рейсы, чтобы вернуть потом в заказе
		OrderResponse result = search(request);
		
		// создаем заказ в ресурсе
		OrderRequest createRequest = createRequest(request);
		OrderResponse response = service.create(createRequest);
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		}
		if (response.getResources() != null
				&& !response.getResources().isEmpty()) {
			
			// заказ для сохранения
			return converter.convertToNewOrder(createRequest, result, response);
		} else {
			throw new ApiException("Empty response");
		}
	}
	
	private OrderResponse search(OrderRequest request) {
		OrderResponse response = new OrderResponse();
		response.setId(request.getId());
		response.setCustomers(request.getCustomers());
		response.setServices(new ArrayList<>());
		response.setVehicles(new HashMap<>());
		response.setOrganisations(new HashMap<>());
		response.setLocalities(new HashMap<>());
		response.setSegments(new HashMap<>());
		TripSearchResponse search = searchController.search(request,
				request.getServices().stream().map(service -> service.getSegment().getId()).collect(Collectors.toSet()));
		if (search != null) {
			if (search.getVehicles() != null) {
				response.getVehicles().putAll(search.getVehicles());
			}
			if (search.getOrganisations() != null) {
				response.getOrganisations().putAll(search.getOrganisations());
			}
			response.getLocalities().putAll(search.getLocalities());
			response.getSegments().putAll(search.getSegments());
		}
		if (response.getVehicles().isEmpty()) {
			response.setVehicles(null);
		}
		if (response.getOrganisations().isEmpty()) {
			response.setOrganisations(null);
		}
		if (response.getLocalities().isEmpty()) {
			response.setLocalities(null);
		}
		if (response.getSegments().isEmpty()) {
			response.setSegments(null);
		}
		return response;
	}
	
	private OrderRequest createRequest(OrderRequest request) {
		List<Resource> resources = getResources();
		Map<Long, OrderRequest> requests = new HashMap<>();
		for (ServiceItem item : request.getServices()) {
			TripIdModel idModel = new TripIdModel().create(item.getSegment().getId());
			
			// проверяем ресурс
			Resource serviceResource = getResource(idModel.getResourceId(), resources);
			if (serviceResource == null) {
				throw new ResourceUnavailableException("Resource is unavailable for service where segmentId="
						+ item.getSegment().getId());
			}
			// проверяем доступность метода
			if (!infoController.isMethodAvailable(serviceResource, Method.ORDER, MethodType.POST)) {
				throw new MethodUnavalaibleException("Method is unavailable for service where segmentId="
						+ item.getSegment().getId());
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
			item.getSegment().setId(idModel.getId());
			resourceRequest.getServices().add(item);
		}
		OrderRequest newRequest = new OrderRequest();
		newRequest.setCustomers(request.getCustomers());
		newRequest.setResources(new ArrayList<>(requests.values()));
		return newRequest;
	}
	
	private List<OrderRequest> operationRequests(Order order, String method, Set<Status> statuses) {
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
	
	private Set<Status> getStatusesForBooking() {
		Set<Status> statuses = new HashSet<>();
		statuses.add(Status.NEW);
		statuses.add(Status.BOOKING_ERROR);
		return statuses;
	}
	
	private Set<Status> getStatusesForConfirm() {
		Set<Status> statuses = new HashSet<>();
		statuses.add(Status.NEW);
		statuses.add(Status.BOOKING);
		statuses.add(Status.BOOKING_ERROR);
		statuses.add(Status.CONFIRM_ERROR);
		return statuses;
	}
	
	private Set<Status> getStatusesForReturn() {
		Set<Status> statuses = new HashSet<>();
		statuses.add(Status.CONFIRM);
		statuses.add(Status.RETURN_ERROR);
		return statuses;
	}
	
	private Set<Status> getStatusesForCancel() {
		Set<Status> statuses = new HashSet<>();
		statuses.add(Status.NEW);
		statuses.add(Status.CONFIRM_ERROR);
		statuses.add(Status.CONFIRM);
		statuses.add(Status.BOOKING);
		statuses.add(Status.BOOKING_ERROR);
		statuses.add(Status.CANCEL_ERROR);
		return statuses;
	}
	
	private void checkStatus(Order order, Set<Status> statuses) {
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (isStatus(statuses, resourceOrder)) {
				return;
			}
		}
		throw new MethodUnavalaibleException("Order status is not " + Status.NEW);
	}
	
	private boolean isStatus(Set<Status> statuses, ResourceOrder resourceOrder) {
		
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
		Order order = findOrder(orderId, Status.BOOKING);
		
		// проверяем статус заказа. выкупить можно NEW, RESERV_ERROR
		Set<Status> statuses = getStatusesForBooking();
		checkStatus(order, statuses);
		
		List<OrderRequest> requests = operationRequests(order, Method.ORDER_BOOKING, statuses);
		
		// подтверждаем в ресурсах
		List<OrderResponse> responses = service.booking(requests);
		
		// преобразовываем и сохраняем
		order = converter.convertToConfirm(order, requests, responses, Status.BOOKING, Status.BOOKING_ERROR);
		try {
			manager.booking(order);
		} catch (ManageException e) {
			LOGGER.error("Booking order error in db", e);
		}
		return converter.getResponse(order);
	}
	
	public OrderResponse confirm(long orderId) {
		Order order = findOrder(orderId, Status.CONFIRM);
		
		// проверяем статус заказа. выкупить можно NEW, RESERV, RESERV_ERROR, CONFIRM_ERROR
		Set<Status> statuses = getStatusesForConfirm();
		checkStatus(order, statuses);
		
		List<OrderRequest> requests = operationRequests(order, Method.ORDER_CONFIRM, statuses);
		
		// подтверждаем в ресурсах
		List<OrderResponse> responses = service.confirm(requests);
		
		// преобразовываем и сохраняем
		order = converter.convertToConfirm(order, requests, responses, Status.CONFIRM, Status.CONFIRM_ERROR);
		try {
			manager.confirm(order);
		} catch (ManageException e) {
			LOGGER.error("Confirm order error in db", e);
		}
		return converter.getResponse(order);
	}
	
	public OrderResponse cancel(long orderId) {
		Order order = findOrder(orderId, Status.CANCEL);

		// проверяем статус заказа. аннулировать можно NEW, CONFIRM_ERROR, RESERVE, RESERVE_ERROR, CONFIRM, CANCEL_ERROR
		Set<Status> statuses = getStatusesForCancel();
		checkStatus(order, statuses);
		
		List<OrderRequest> requests = operationRequests(order, Method.ORDER_CANCEL, statuses);
		
		// подтверждаем в ресурсах
		List<OrderResponse> responses = service.cancel(requests);
		
		// преобразовываем и сохраняем
		order = converter.convertToConfirm(order, requests, responses, Status.CANCEL, Status.CANCEL_ERROR);
		try {
			manager.cancel(order);
		} catch (ManageException e) {
			LOGGER.error("Cancel order error in db", e);
		}
		return converter.getResponse(order);
	}
	
	public OrderResponse getOrder(long orderId) {
		Order order = findOrder(orderId, null, "Order is unavailable");
		return converter.getResponse(order);
	}
	
	public OrderResponse getService(long serviceId) {
		OrderParams params = new OrderParams();
		params.setServiceId(serviceId);
		Order order = findOrder(params);
		if (!dataController.isOrderAvailable(order, null)) {
			throw new NoDataFoundException("Order is unavailable");
		}
		return converter.getService(converter.getResponse(order), serviceId);
	}
	
	private Order findOrder(long orderId, Status status) {
		return findOrder(orderId, status, "Operation is unavailable on order for this user");
	}
	
	private Order findOrder(long orderId, Status status, String unavailableMessage) {
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
		Order order = findOrder(orderId, Status.NEW);
		
		// проверяем статус заказа. добавить в заказ можно, если он в статусе NEW, RESERV, RESERV_ERROR, CONFIRM_ERROR
		checkStatus(order, getStatusesForConfirm());
		
		// создаем заказ в ресурсах, объединяем с имеющимся и сохраняем 
		Order newOrder = createInResource(request);
		order = converter.joinOrders(order, newOrder);
		try {
			order = manager.addServices(order);
		} catch (ManageException e) {
			LOGGER.error("Add services to order error in db", e);
		}
		return converter.getResponse(order);
	}
	
	public OrderResponse removeService(long orderId, OrderRequest request) {
		Order order = findOrder(orderId, Status.NEW);

		// проверяем статус заказа. удалить из заказа можно, если он в статусе NEW, RESERV, RESERV_ERROR, CONFIRM_ERROR
		checkStatus(order, getStatusesForConfirm());
		
		// создаем заказ в ресурсах, объединяем с имеющимся и сохраняем 
		order = converter.removeServices(order, request.getServices());
		try {
			order = manager.removeServices(order);
		} catch (ManageException e) {
			LOGGER.error("Remove services from order error in db", e);
		}
		return converter.getResponse(order);
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
		if (!dataController.isOrderAvailable(order, null)) {
			throw new NoDataFoundException("Order is unavailable");
		}
		if (order.getDocuments() != null
				&& !order.getDocuments().isEmpty()) {
			return converter.getDocumentsResponse(order);
		}
		// если нет, то берем у ресурсов и сохраняем что есть
		List<OrderRequest> requests = operationRequests(order, Method.ORDER_DOCUMENTS, null);
		
		// получаем документы в ресурсах
		List<OrderResponse> responses = service.getPdfDocuments(requests);
		
		// сохраняем документы по заказу
		order = converter.addDocuments(order, responses);
		try {
			order = manager.update(order);
		} catch (ManageException e) {
			LOGGER.error("Save documents to order error in db", e);
		}
		return converter.getDocumentsResponse(order);
	}
	
	public OrderResponse calcReturn(long orderId, OrderRequest request) {
		Order order = findOrder(orderId, Status.RETURN);
		
		// проверяем статус заказа. выкупить можно CONFIRM, RETURN_ERROR
		checkStatus(order, getStatusesForReturn());
		List<OrderRequest> requests = returnRequests(order, request, Method.ORDER_RETURN_PREPARE);
		
		List<OrderResponse> responses = service.prepareReturnServices(requests);
		
		return converter.convertToReturnCalc(order, requests, responses, Status.RETURN, Status.RETURN_ERROR);
	}
	
	public OrderResponse confirmReturn(long orderId, OrderRequest request) {
		Order order = findOrder(orderId, Status.RETURN);
		
		// проверяем статус заказа. выкупить можно CONFIRM, RETURN_ERROR
		checkStatus(order, getStatusesForReturn());
		List<OrderRequest> requests = returnRequests(order, request, Method.ORDER_RETURN_PREPARE);
		
		return null;
	}
	
	private List<OrderRequest> returnRequests(Order order, OrderRequest request, String method) {
		Set<Status> statuses = getStatusesForReturn();
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
									errorMsg.append("Service id=").append(service.getId()).append(" status is not ").append(Status.CONFIRM).append("\r\n");
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
	
}
