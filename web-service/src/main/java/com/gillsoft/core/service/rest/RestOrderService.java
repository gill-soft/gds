package com.gillsoft.core.service.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.model.Method;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.model.service.OrderService;

public class RestOrderService implements OrderService {
	
	private RestResourceService resourceService;

	@Override
	public OrderResponse create(OrderRequest request) {
		return sendPostRequest(Method.ORDER, request, new LinkedMultiValueMap<>(0), OrderResponse.class);
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
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("orderId", orderId);
		return sendPostRequest(Method.ORDER_CONFIRM, null, params, OrderResponse.class);
	}
	
	@Override
	public OrderResponse cancel(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public OrderResponse prepareReturnServices(OrderRequest request) {
		return sendPostRequest(Method.ORDER_RETURN_PREPARE, request, new LinkedMultiValueMap<>(0), OrderResponse.class);
	}

	@Override
	public OrderResponse returnServices(OrderRequest request) {
		return sendPostRequest(Method.ORDER_RETURN_CONFIRM, request, new LinkedMultiValueMap<>(0), OrderResponse.class);
	}

	@Override
	public OrderResponse getPdfDocuments(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}
	
	private <T> T sendPostRequest(String method, OrderRequest request, MultiValueMap<String, String> params,
			Class<T> type) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + method)
				.queryParams(params)
				.build().toUri();
		ResponseEntity<T> response = resourceService.getTemplate()
				.postForEntity(uri, request, type);
		return response.getBody();
	}
	
}
