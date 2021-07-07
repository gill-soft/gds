package com.gillsoft.control.core;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gillsoft.control.core.OrderResponseHelper.ServiceOrderResponseHandler;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.model.AdditionalServiceEmptyResource;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.model.AdditionalServiceItem;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

@Component
public class AdditionalServiceOrderResponseHandler implements ServiceOrderResponseHandler {
	
	@Autowired
	private OrderResponseConverter orderResponseConverter;
	
	@Autowired
	private MsDataController dataController;

	@Override
	public void beforeOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resourcesRequests,
			OrderResponse resourcesResponses) {
		
	}

	@Override
	public void beforeServices(OrderResponse result, OrderRequest resourceRequest, OrderResponse resourceResponse) {
		
	}

	@Override
	public void beforeService(OrderResponse result, ServiceItem serviceItem, OrderRequest resourceRequest,
			OrderResponse resourceResponse) {
		AdditionalServiceItem additionalServiceItem = serviceItem.getAdditionalService();
		if (additionalServiceItem != null) {
			if (serviceItem.getPrice() != null) {
				serviceItem.setPrice(dataController.recalculate(serviceItem.getPrice(), resourceRequest.getCurrency()));
			} else if (result.getAdditionalServices() != null) {
				AdditionalServiceItem additionalService = result.getAdditionalServices().get(additionalServiceItem.getId());
				if (additionalService != null) {
					serviceItem.setPrice(additionalService.getPrice());
					orderResponseConverter.addReturnConditions(serviceItem, additionalService.getPrice());
				}
			}
			String id = new IdModel(new AdditionalServiceEmptyResource().getId(), additionalServiceItem.getId()).asString();
			if (result.getAdditionalServices() == null) {
				result.setAdditionalServices(new HashMap<>());
			}
			result.getAdditionalServices().put(id, resourceResponse.getAdditionalServices().get(additionalServiceItem.getId()));
			if (additionalServiceItem.getEnableReturn() == null) {
				additionalServiceItem.setEnableReturn(true);
			}
			if (additionalServiceItem.getEnableCount() == null) {
				additionalServiceItem.setEnableCount(true);
			}
			additionalServiceItem.setId(id);
		}
	}

	@Override
	public void afterService(OrderResponse result, ServiceItem serviceItem, ResourceService resourceService,
			OrderRequest resourceRequest, OrderResponse resourceResponse) {
		
	}

	@Override
	public void afterServices(OrderResponse result, OrderRequest resourceRequest, OrderResponse resourceResponse) {
		
	}

	@Override
	public void afterOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resourcesRequests,
			OrderResponse resourcesResponses, Order order) {
		
	}
	
}
