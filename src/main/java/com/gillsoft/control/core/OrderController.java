package com.gillsoft.control.core;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.NoDataFoundException;
import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Method;
import com.gillsoft.model.PaymentMethod;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.Resource;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderController {
	
	private static Logger LOGGER = LogManager.getLogger(OrderController.class);
	
	@Autowired
	private AgregatorOrderService agregatorService;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private TripSearchController searchController;
	
	@Autowired
	private OrderRequestValidator validator;
	
	@Autowired
	private OrderRequestController orderRequestController;
	
	@Autowired
	private OrderResponseConverter orderConverter;
	
	@Autowired
	private OrderDocumentController documentController;
	
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
		OrderRequest createRequest = orderRequestController.createRequest(request);
		OrderResponse response = agregatorService.create(createRequest);
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		}
		if (response.getResources() != null
				&& !response.getResources().isEmpty()) {
			
			// заказ для сохранения
			return orderConverter.convertToNewOrder(request, createRequest, result, response);
		} else {
			throw new ApiException("Empty response");
		}
	}
	
	
	/*
	 * Сохраняем и устанавливаем все необходимые поля.
	 */
	private OrderResponse saveOrder(Order order) {
		List<Resource> resources = dataController.getUserResources();
		if (resources == null) {
			throw new ResourceUnavailableException("User does not has available resources");
		}
		try {
			return orderConverter.getResponse(manager.create(order));
		} catch (ManageException e) {
			LOGGER.error(e);
			throw new ApiException(e);
		}
	}
	
	public OrderResponse booking(long orderId) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
		
			Order order = findOrder(orderId, ServiceStatus.BOOKING);
			List<OrderRequest> requests = orderRequestController.createBookingRequests(order);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// подтверждаем в ресурсах
			List<OrderResponse> responses = agregatorService.booking(requests);
			
			// преобразовываем и сохраняем
			order = orderConverter.convertToConfirm(order, requests, responses, ServiceStatus.BOOKING, ServiceStatus.BOOKING_ERROR);
			try {
				manager.booking(order);
			} catch (ManageException e) {
				LOGGER.error("Booking order error in db", e);
			}
			return orderConverter.getResponse(order);
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
			List<OrderRequest> requests = orderRequestController.createConfirmRequests(order);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// подтверждаем в ресурсах
			List<OrderResponse> responses = agregatorService.confirm(requests);
			
			// преобразовываем и сохраняем
			order = orderConverter.convertToConfirm(order, requests, responses, ServiceStatus.CONFIRM, ServiceStatus.CONFIRM_ERROR);
			if (paymentMethod != null) {
				order.setPayment(paymentMethod);
			}
			try {
				manager.confirm(order);
			} catch (ManageException e) {
				LOGGER.error("Confirm order error in db", e);
			}
			return orderConverter.getResponse(order);
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
			List<OrderRequest> requests = orderRequestController.createCancelRequests(order);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// подтверждаем в ресурсах
			List<OrderResponse> responses = agregatorService.cancel(requests);
			
			// преобразовываем и сохраняем
			order = orderConverter.convertToConfirm(order, requests, responses, ServiceStatus.CANCEL, ServiceStatus.CANCEL_ERROR);
			order.setCancelReason(reason);
			try {
				manager.cancel(order);
			} catch (ManageException e) {
				LOGGER.error("Cancel order error in db", e);
			}
			return orderConverter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	public OrderResponse getOrder(long orderId) {
		Order order = findOrder(orderId, ServiceStatus.VIEW, "Order is unavailable");
		return orderConverter.getResponse(order);
	}
	
	public OrderResponse getService(long serviceId) {
		OrderParams params = new OrderParams();
		params.setServiceId(serviceId);
		Order order = findOrderPart(params);
		if (!dataController.isOrderAvailable(order, ServiceStatus.VIEW)) {
			throw new NoDataFoundException("Order is unavailable");
		}
		return orderConverter.getService(orderConverter.getResponse(order), serviceId);
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
			order = manager.getFullOrder(params);
		} catch (ManageException e) {
			LOGGER.error("Find order error in db", e);
		}
		if (order == null) {
			throw new NoDataFoundException("Order not found");
		}
		return order;
	}
	
	private Order findOrderPart(OrderParams params) {
		Order order = null;
		try {
			order = manager.getOrderPart(params);
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
			orderRequestController.checkStatus(order, orderRequestController.getStatusesForConfirm());
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// создаем заказ в ресурсах, объединяем с имеющимся и сохраняем 
			Order newOrder = createInResource(request);
			order = orderConverter.joinOrders(order, newOrder);
			try {
				order = manager.addServices(order);
			} catch (ManageException e) {
				LOGGER.error("Add services to order error in db", e);
			}
			return orderConverter.getResponse(order);
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
			orderRequestController.checkStatus(order, orderRequestController.getStatusesForConfirm());
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
			
			// создаем заказ в ресурсах, объединяем с имеющимся и сохраняем 
			order = orderConverter.removeServices(order, request.getServices());
			try {
				order = manager.removeServices(order);
			} catch (ManageException e) {
				LOGGER.error("Remove services from order error in db", e);
			}
			return orderConverter.getResponse(order);
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
	
	public OrderResponse getDocuments(long orderId, Lang lang) {
		
		// проверяем билеты в заказе
		Order order = getOrderDocuments(orderId);
		if (!dataController.isOrderAvailable(order, ServiceStatus.VIEW)) {
			throw new NoDataFoundException("Order is unavailable");
		}
		if (order.getDocuments() != null
				&& !order.getDocuments().isEmpty()) {
			documentController.addSystemDocuments(order, lang);
			return orderConverter.getDocumentsResponse(order);
		}
		try {
			// если нет, то берем у ресурсов и сохраняем что есть
			List<OrderRequest> requests = orderRequestController.operationRequests(order, Method.ORDER_DOCUMENTS, null);
			
			// получаем документы в ресурсах
			List<OrderResponse> responses = agregatorService.getPdfDocuments(requests);
			
			// сохраняем документы по заказу
			order = orderConverter.addDocuments(order, requests, responses);
			try {
				order = manager.update(order);
			} catch (ManageException e) {
				LOGGER.error("Save documents to order error in db", e);
			}
		} catch (Exception e) {
			LOGGER.error("Can not get documents from resource.", e);
		}
		documentController.addSystemDocuments(order, lang);
		return orderConverter.getDocumentsResponse(order);
	}
	
	public OrderResponse calcReturn(long orderId, OrderRequest request) {
		Order order = findOrder(orderId, ServiceStatus.RETURN);
		List<OrderRequest> requests = orderRequestController.createPrepareReturnRequests(order, request);
		List<OrderResponse> responses = agregatorService.prepareReturnServices(requests);
		return orderConverter.convertToReturnCalc(order, requests, responses);
	}
	
	public OrderResponse confirmReturn(long orderId, OrderRequest request) {
		try {
			// блокировка заказа
			String lockId = locker.lock(orderId);
		
			Order order = findOrder(orderId, ServiceStatus.RETURN);
			List<OrderRequest> requests = orderRequestController.createReturnRequests(order, request);
			
			// получаем стоимости возврата на случай, если они не будут возвращены в самом возврате
			List<OrderResponse> calcResponses = agregatorService.prepareReturnServices(requests);
			
			// проверка блокировки
			locker.checkLock(orderId, lockId);
						
			// возвращаем сервисы
			List<OrderResponse> returnResponses = agregatorService.returnServices(requests);
			order = orderConverter.convertToReturn(order, requests, returnResponses, calcResponses);
			try {
				manager.returnServices(order);
			} catch (ManageException e) {
				LOGGER.error("Return services error in db", e);
			}
			return orderConverter.getResponse(order);
		} finally {
			
			// разблокировка заказа
			locker.unlock(orderId);
		}
	}
	
	public List<Order> getOrders(int count) {
		
		// возвращать не больше 1000 заказов
		if (count > 1000) {
			throw new RequestValidateException("To many rows by request.");
		}
		OrderParams params = new OrderParams();
		params.setCount(count);
		params.setReported(false);
		params.setMappedTrip(true);
		try {
			List<Order> orders = orderConverter.addPrice(manager.getOrders(params));
			
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
	
	public List<OrderResponse> getActiveOrders() {
		OrderParams params = new OrderParams();
		params.setDepartureFrom(new Date()); //TODO convert to departure city timezone
		params.setUserId(dataController.getUser().getId());
		params.setStatuses(Arrays.asList(ServiceStatus.NEW,
				ServiceStatus.CONFIRM,
				ServiceStatus.CONFIRM_ERROR,
				ServiceStatus.BOOKING,
				ServiceStatus.RETURN,
				ServiceStatus.RETURN_ERROR,
				ServiceStatus.CANCEL_ERROR));
		try {
			List<Order> orders = manager.getOrders(params);
			return orders.stream().map(o -> orderConverter.getResponse(o)).collect(Collectors.toList());
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
