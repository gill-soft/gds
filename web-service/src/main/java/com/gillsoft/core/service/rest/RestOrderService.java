package com.gillsoft.core.service.rest;

import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.model.service.OrderService;

public class RestOrderService implements OrderService {
	
	private RestResourceService resourceService;

	@Override
	public OrderResponse create(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse addTickets(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse removeTickets(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse updatePassengers(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse get(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse getTicket(String ticketId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse book(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse pay(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse cancel(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse returnTickets(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse getPdfTickets(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}
	
}
