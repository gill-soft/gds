package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.OrderResponse;
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
	private MappingService mappingService;
	
	@Autowired
	private TripSearchMapping tripSearchMapping;
	
	public OrderResponse create(OrderRequest request) {
		
		// проверяем параметры запроса
		validateOrderRequest(request);
		
		OrderRequest createRequest = createRequest(request);
		OrderResponse response = service.create(createRequest);
		
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		}
		if (response.getResources() != null
				&& !response.getResources().isEmpty()) {
			
			// преобразовываем ответ
			OrderResponse result = new OrderResponse();
			result.setId(request.getId());
			result.setCustomers(request.getCustomers());
			result.setServices(new ArrayList<>());
			// TODO orderId
			for (OrderResponse orderResponse : response.getResources()) {
				Stream<OrderRequest> stream = createRequest.getResources().stream().filter(r -> r.getId().equals(orderResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					OrderRequest currRequest = stream.findFirst().get();
					for (ServiceItem item : orderResponse.getServices()) {
						
					}
				}
			}
			
			return result;
		} else {
			throw new ApiException("Empty response");
		}
	}
	
	private void validateOrderRequest(OrderRequest request) {
		
		// проверяем кастомеров
		if (request.getCustomers() == null
				|| request.getCustomers().isEmpty()) {
			throw new RequestValidateException("Empty customers");
		}
		// проверяем сервисы
		if (request.getServices() == null
				|| request.getServices().isEmpty()) {
			throw new RequestValidateException("Empty services");
		}
		for (ServiceItem item : request.getServices()) {
			
			// проверяем сегменты
			if (item.getSegment() == null) {
				throw new RequestValidateException("Empty segment");
			}
			if (item.getSegment().getId() == null) {
				throw new RequestValidateException("Segment part is present but empty id property");
			}
			// проверяем кастомеров
			if (item.getCustomer() == null) {
				throw new RequestValidateException("Empty customer");
			}
			if (item.getCustomer().getId() == null) {
				throw new RequestValidateException("Customer part is present but empty id property");
			}
			// проверяем места
			if (item.getSeat() != null
					&& item.getSeat().getId() == null) {
				throw new RequestValidateException("Seat part is present but empty id property");
			}
			// проверяем тариф
			if (item.getPrice() != null) {
				if (item.getPrice().getTariff() == null) {
					throw new RequestValidateException("Price part is present but empty tariff part");
				}
				if (item.getPrice().getTariff().getId() == null) {
					throw new RequestValidateException("Tariff part is present but empty id property");
				}
			}
		}
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
					resourceRequest.setParams(serviceResource.createParams());
					resourceRequest.setServices(new ArrayList<>());
					requests.put(serviceResource.getId(), resourceRequest);
				}
				item.getSegment().setId(idModel.getId());
				resourceRequest.getServices().add(item);
			}
			OrderRequest newRequest = new OrderRequest();
			return newRequest;
		}
		throw new ResourceUnavailableException("User does not has available resources");
	}

}
