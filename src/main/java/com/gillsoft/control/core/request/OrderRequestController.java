package com.gillsoft.control.core.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.core.IdModel;
import com.gillsoft.control.core.OrderResponseConverter;
import com.gillsoft.control.core.ResourceInfoController;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderRequestController {
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private OrderResponseConverter orderConverter;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private List<ServiceOrderRequestHandler> createOrderRequests;
	
	public OrderRequest createRequest(OrderRequest request) {
		OrderRequest newRequest = new OrderRequest();
		newRequest.setCustomers(request.getCustomers());
		newRequest.setResources(new ArrayList<>());
		for (ServiceOrderRequestHandler createOrderRequest : createOrderRequests) {
			newRequest.getResources().addAll(createOrderRequest.create(request));
		}
		return newRequest;
	}
	
	public List<OrderRequest> operationRequests(Order order, String method, Set<ServiceStatus> statuses) {
		return operationRequests(order, method, statuses, false);
	}
	
	public List<OrderRequest> operationRequests(Order order, String method, Set<ServiceStatus> statuses, boolean ignoreException) {
		List<OrderRequest> requests = new ArrayList<>();
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (statuses == null
					|| isStatus(statuses, resourceOrder)) {
			
				// проверяем ресурс
				Resource serviceResource = dataController.getResource(resourceOrder.getResourceId());
				if (serviceResource == null) {
					throw new ResourceUnavailableException("Resource is unavailable for service ids "
							+ Arrays.toString(resourceOrder.getServices().stream().map(ResourceService::getId)
									.collect(Collectors.toList()).toArray()));
				}
				// проверяем доступность метода
				if (!infoController.isMethodAvailable(serviceResource, method, MethodType.POST)) {
					if (ignoreException) {
						continue;
					}
					throw new MethodUnavalaibleException("Method is unavailable for service ids "
							+ Arrays.toString(resourceOrder.getServices().stream().map(ResourceService::getId)
									.collect(Collectors.toList()).toArray()));
				}
				OrderRequest resourceRequest = new OrderRequest();
				resourceRequest.setId(StringUtil.generateUUID());
				resourceRequest.setParams(createParams(serviceResource, resourceOrder.getResourceParamId()));
				resourceRequest.setOrderId(new IdModel().create(resourceOrder.getResourceNativeOrderId()).getId());
				requests.add(resourceRequest);
			}
		}
		return requests;
	}
	
	private boolean isStatus(Set<ServiceStatus> statuses, ResourceOrder resourceOrder) {
		
		// если последний статус хоть одной продажи находится в списке перечисленных статусов 
		return resourceOrder.getServices().stream().anyMatch(s -> statuses.contains(orderConverter.getLastStatus(s.getStatuses())));
	}
	
	private ResourceParams createParams(Resource resource, long resourceParamsId) {
		ResourceParams params = resource.createParams();
		com.gillsoft.ms.entity.ResourceParams resourceParams = dataController.getResourceParam(resourceParamsId);
		if (resourceParams != null) {
			if (params.getAdditional() == null) {
				params.setAdditional(new ConcurrentHashMap<>());
			}
			params.getAdditional().putAll(resourceParams.getAttributeValues().stream().collect(
					Collectors.toMap(av -> av.getAttribute().getName(), av -> av.getValue(), (av1, av2) -> av1)));
		}
		return params;
	}
	
	public List<OrderRequest> createBookingRequests(Order order) {
		
		// проверяем статус заказа. выкупить можно NEW, RESERV_ERROR
		Set<ServiceStatus> statuses = getStatusesForBooking();
		checkStatus(order, statuses);
		
		return operationRequests(order, Method.ORDER_BOOKING, statuses);
	}
	
	public Set<ServiceStatus> getStatusesForBooking() {
		Set<ServiceStatus> statuses = new HashSet<>();
		statuses.add(ServiceStatus.NEW);
		statuses.add(ServiceStatus.BOOKING_ERROR);
		return statuses;
	}
	
	public void checkStatus(Order order, Set<ServiceStatus> statuses) {
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (isStatus(statuses, resourceOrder)) {
				return;
			}
		}
		throw new MethodUnavalaibleException("Order status is not one of "
				+ String.join(", ", statuses.stream().map(s -> s.name()).collect(Collectors.toSet())));
	}
	
	public void checkStatus(List<OrderResponse> orderResponses, Set<ServiceStatus> statuses) {
		if (isStatus(statuses, orderResponses)) {
			return;
		}
		throw new MethodUnavalaibleException("Order status is not one of "
				+ String.join(", ", statuses.stream().map(s -> s.name()).collect(Collectors.toSet())));
	}
	
	private boolean isStatus(Set<ServiceStatus> statuses, List<OrderResponse> orderResponses) {
		
		// если последний статус хоть одной продажи находится в списке перечисленных статусов 
		return orderResponses.stream().anyMatch(r -> r.getServices().stream().anyMatch(s -> statuses.contains(ServiceStatus.valueOf(s.getStatus()))));
	}
	
	public List<OrderRequest> createConfirmRequests(Order order) {
		
		// проверяем статус заказа. выкупить можно NEW, RESERV, RESERV_ERROR, CONFIRM_ERROR
		Set<ServiceStatus> statuses = getStatusesForConfirm();
		checkStatus(order, statuses);
		
		return operationRequests(order, Method.ORDER_CONFIRM, statuses);
	}
	
	public Set<ServiceStatus> getStatusesForConfirm() {
		Set<ServiceStatus> statuses = new HashSet<>();
		statuses.add(ServiceStatus.NEW);
		statuses.add(ServiceStatus.BOOKING);
		statuses.add(ServiceStatus.BOOKING_ERROR);
		statuses.add(ServiceStatus.CONFIRM_ERROR);
		return statuses;
	}
	
	public Set<ServiceStatus> getStatusesForAddService() {
		Set<ServiceStatus> statuses = getStatusesForConfirm();
		statuses.add(ServiceStatus.CONFIRM);
		return statuses;
	}
	
	public List<OrderRequest> createCancelRequests(Order order) {
		
		// проверяем статус заказа. аннулировать можно NEW, CONFIRM_ERROR, RESERVE, RESERVE_ERROR, CONFIRM, CANCEL_ERROR
		Set<ServiceStatus> statuses = getStatusesForCancel();
		checkStatus(order, statuses);
		
		return operationRequests(order, Method.ORDER_CANCEL, statuses);
	}
	
	public Set<ServiceStatus> getStatusesForCancel() {
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
	
	public List<OrderRequest> createPrepareReturnRequests(Order order, OrderRequest request) {
		
		// проверяем статус заказа. вернуть можно CONFIRM, RETURN_ERROR
		Set<ServiceStatus> statuses = getStatusesForReturn();
		checkStatus(order, statuses);
		
		return returnRequests(order, request, Method.ORDER_RETURN_PREPARE);
	}
	
	public List<OrderRequest> createReturnRequests(Order order, OrderRequest request) {
		
		// проверяем статус заказа. вернуть можно CONFIRM, RETURN_ERROR
		Set<ServiceStatus> statuses = getStatusesForReturn();
		checkStatus(order, statuses);
		
		return returnRequests(order, request, Method.ORDER_RETURN_CONFIRM);
	}
	
	public Set<ServiceStatus> getStatusesForReturn() {
		Set<ServiceStatus> statuses = new HashSet<>();
		statuses.add(ServiceStatus.CONFIRM);
		statuses.add(ServiceStatus.RETURN_ERROR);
		return statuses;
	}
	
	private List<OrderRequest> returnRequests(Order order, OrderRequest request, String method) {
		
		// проверяем возврат скидки
		orderConverter.checkDiscountForReturn(order, request);
		
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
							if (orderConverter.isServiceOfResourceService(service, resourceService)) {
								if (statuses.contains(orderConverter.getLastStatus(resourceService.getStatuses()))) {
									
									// устанавливаем ид ресурса и добавляем в запрос
									ServiceItem requestService = (ServiceItem) SerializationUtils.deserialize(SerializationUtils.serialize(service));
									requestService.setId(new IdModel().create(resourceService.getResourceNativeServiceId()).getId());
									if (orderRequest.getServices() == null) {
										orderRequest.setServices(new ArrayList<>());
									}
									orderRequest.getServices().add(requestService);
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
	
	public static interface ServiceOrderRequestHandler {
		
		public List<OrderRequest> create(OrderRequest request);

	}

}
