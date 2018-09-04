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
		return sendPostRequest(Method.ORDER, request, new LinkedMultiValueMap<>(0));
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
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("orderId", orderId);
		return sendGetRequest(Method.ORDER, params);
	}

	@Override
	public OrderResponse getService(String serviceId) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("serviceId", serviceId);
		return sendGetRequest(Method.ORDER_SERVICE, params);
	}

	@Override
	public OrderResponse booking(String orderId) {
		return confirmMethod(orderId, Method.ORDER_BOOKING);
	}

	@Override
	public OrderResponse confirm(String orderId) {
		return confirmMethod(orderId, Method.ORDER_CONFIRM);
	}
	
	@Override
	public OrderResponse cancel(String orderId) {
		return confirmMethod(orderId, Method.ORDER_CANCEL);
	}
	
	private OrderResponse confirmMethod(String orderId, String method) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(1);
		params.add("orderId", orderId);
		return sendPostRequest(method, null, params);
	}
	
	@Override
	public OrderResponse prepareReturnServices(OrderRequest request) {
		return sendPostRequest(Method.ORDER_RETURN_PREPARE, request, new LinkedMultiValueMap<>(0));
	}

	@Override
	public OrderResponse returnServices(OrderRequest request) {
		return sendPostRequest(Method.ORDER_RETURN_CONFIRM, request, new LinkedMultiValueMap<>(0));
	}

	@Override
	public OrderResponse getPdfDocuments(OrderRequest request) {
		return sendPostRequest(Method.ORDER_DOCUMENTS, request, new LinkedMultiValueMap<>(0));
	}

	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}
	
	private OrderResponse sendPostRequest(String method, OrderRequest request, MultiValueMap<String, String> params) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + method)
				.queryParams(params).build().toUri();
		ResponseEntity<OrderResponse> response = resourceService.getTemplate().postForEntity(uri, request, OrderResponse.class);
		return response.getBody();
	}
	
	private OrderResponse sendGetRequest(String method, MultiValueMap<String, String> params) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + method)
				.queryParams(params).build().toUri();
		ResponseEntity<OrderResponse> response = resourceService.getTemplate().getForEntity(uri, OrderResponse.class);
		return response.getBody();
	}
	
}
