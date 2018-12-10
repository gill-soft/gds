package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
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
		if (search.getVehicles() != null) {
			response.getVehicles().putAll(search.getVehicles());
		}
		if (search.getOrganisations() != null) {
			response.getOrganisations().putAll(search.getOrganisations());
		}
		response.getLocalities().putAll(search.getLocalities());
		response.getSegments().putAll(search.getSegments());
		return response;
	}
	
	private OrderRequest createRequest(OrderRequest request) {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			Map<Long, OrderRequest> requests = new HashMap<>();
			for (ServiceItem item : request.getServices()) {
				TripIdModel idModel = new TripIdModel().create(item.getSegment().getId());
				
				// проверяем ресурс
				Resource serviceResource = null;
				for (Resource resource : resources) {
					if (idModel.getResourceId() == resource.getId()) {
						serviceResource = resource;
					}
				}
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
		throw new ResourceUnavailableException("User does not has available resources");
	}
	
	/*
	 * Сохраняем и устанавливаем все необходимые поля.
	 */
	private OrderResponse saveOrder(Order order) {
		try {
			return converter.getResponse(manager.create(order));
		} catch (ManageException e) {
			LOGGER.error("Error when save order", e);
			throw new ApiException(e);
		}
	}

}
