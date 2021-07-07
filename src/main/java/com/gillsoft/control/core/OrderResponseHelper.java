package com.gillsoft.control.core;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class OrderResponseHelper {
	
	@Autowired
	private List<ServiceOrderResponseHandler> responseHandlers;

	public void beforeOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resourcesRequests,
			OrderResponse resourcesResponses) {
		for (ServiceOrderResponseHandler newOrderHandler : responseHandlers) {
			newOrderHandler.beforeOrder(originalRequest, result, resourcesRequests, resourcesResponses);
		}
	}
	
	public void beforeServices(OrderResponse result, OrderRequest resourceRequest, OrderResponse resourceResponse) {
		for (ServiceOrderResponseHandler newOrderHandler : responseHandlers) {
			newOrderHandler.beforeServices(result, resourceRequest, resourceResponse);
		}
	}
	
	public void beforeService(OrderResponse result, ServiceItem serviceItem, OrderRequest resourceRequest, OrderResponse resourceResponse) {
		for (ServiceOrderResponseHandler newOrderHandler : responseHandlers) {
			newOrderHandler.beforeService(result, serviceItem, resourceRequest, resourceResponse);
		}
	}
	
	public void afterService(OrderResponse result, ServiceItem serviceItem, ResourceService resourceService,
			OrderRequest resourceRequest, OrderResponse resourceResponse) {
		for (ServiceOrderResponseHandler newOrderHandler : responseHandlers) {
			newOrderHandler.afterService(result, serviceItem, resourceService, resourceRequest, resourceResponse);
		}
	}
	
	public void afterServices(OrderResponse result, OrderRequest resourceRequest, OrderResponse resourceResponse) {
		for (ServiceOrderResponseHandler newOrderHandler : responseHandlers) {
			newOrderHandler.afterServices(result, resourceRequest, resourceResponse);
		}
	}
	
	public void afterOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resourcesRequests,
			OrderResponse resourcesResponses, Order order) {
		for (ServiceOrderResponseHandler newOrderHandler : responseHandlers) {
			newOrderHandler.afterOrder(originalRequest, result, resourcesRequests, resourcesResponses, order);
		}
	}
	
	public static interface ServiceOrderResponseHandler {
		
		public void beforeOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resourcesRequests,
				OrderResponse resourcesResponses);
		
		public void beforeServices(OrderResponse result, OrderRequest resourceRequest, OrderResponse resourceResponse);
		
		public void beforeService(OrderResponse result, ServiceItem serviceItem, OrderRequest resourceRequest, OrderResponse resourceResponse);
		
		public void afterService(OrderResponse result, ServiceItem serviceItem, ResourceService resourceService,
				OrderRequest resourceRequest, OrderResponse resourceResponse);
		
		public void afterServices(OrderResponse result, OrderRequest resourceRequest, OrderResponse resourceResponse);
		
		public void afterOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resourcesRequests,
				OrderResponse resourcesResponses, Order order);
		
	}
	
}
