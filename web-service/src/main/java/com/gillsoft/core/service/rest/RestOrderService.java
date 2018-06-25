package com.gillsoft.core.service.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.model.Method;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.model.service.OrderService;

public class RestOrderService implements OrderService {
	
	private RestResourceService resourceService;

	@Override
	public OrderResponse create(OrderRequest request) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.ORDER)
				.build().toUri();
		ResponseEntity<OrderResponse> response = resourceService.getTemplate()
				.postForEntity(uri, request, OrderResponse.class);
		return response.getBody();
	}

	@Override
	public OrderResponse addServices(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse removeServices(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse updateCustomers(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse get(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse getService(String serviceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse booking(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse confirm(String orderId) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.ORDER_CONFIRM)
				.queryParam("orderId", orderId)
				.build().toUri();
		ResponseEntity<OrderResponse> response = resourceService.getTemplate()
				.postForEntity(uri, null, OrderResponse.class);
		return response.getBody();
	}

	@Override
	public OrderResponse cancel(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public OrderResponse prepareReturnServices(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse returnServices(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse getPdfDocuments(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}
	
}
