package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import com.gillsoft.ms.entity.User;
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
			return saveOrder(converter.convertToNewOrder(createRequest, result, response));
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
	
	private List<OrderRequest> confirmRequests(Order order) {
		List<Resource> resources = getResources();
		List<OrderRequest> requests = new ArrayList<>();
		
		Set<Status> statuses = getNewOrderStatuses();
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (isStatus(statuses, resourceOrder)) {
			
				// проверяем ресурс
				Resource serviceResource = getResource(resourceOrder.getResourceId(), resources);
				if (serviceResource == null) {
					throw new ResourceUnavailableException("Resource is unavailable for service ids "
							+ Arrays.toString(resourceOrder.getServices().stream().map(ResourceService::getId)
									.collect(Collectors.toList()).toArray()));
				}
				// проверяем доступность метода
				if (!infoController.isMethodAvailable(serviceResource, Method.ORDER_CONFIRM, MethodType.POST)) {
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
	
	private Set<Status> getNewOrderStatuses() {
		Set<Status> statuses = new HashSet<>();
		statuses.add(Status.NEW);
		statuses.add(Status.CONFIRM_ERROR);
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
		return resourceOrder.getServices().stream().anyMatch(s -> statuses.contains(s.getStatuses().iterator().next().getStatus()));
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
	
	public OrderResponse confirm(long orderId) {
		Order order = findOrder(orderId);
		
		// проверяем статус заказа NEW и CONFIRM_ERROR
		checkStatus(order, getNewOrderStatuses());
		
		List<OrderRequest> requests = confirmRequests(order);
		
		// подтверждаем в ресурсах
		List<OrderResponse> responses = service.confirm(requests);
		
		// преобразовываем и сохраняем
		OrderResponse response = converter.convertToConfirm(order, requests, responses);
		try {
			manager.confirm(order);
		} catch (ManageException e) {
			LOGGER.error("Confirm order error", e);
		}
		return response;
	}
	
	public OrderResponse getOrder(long orderId) {
		return converter.getResponse(findOrder(orderId));
	}
	
	public Order findOrder(long orderId) {
		OrderParams params = new OrderParams();
		User user = dataController.getUser();
		params.setUserId(user.getId());
		params.setOrderId(orderId);
		Order order = null;
		try {
			order = manager.get(params);
		} catch (ManageException e) {
			LOGGER.error(e);
		}
		if (order == null) {
			throw new NoDataFoundException("Order not found or unavailable");
		}
		return order;
	}

}
