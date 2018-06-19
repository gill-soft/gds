package com.gillsoft.model.service;

import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

public interface OrderService {
	
	public OrderResponse create(OrderRequest request);
	
	public OrderResponse addTickets(OrderRequest request);
	
	public OrderResponse removeTickets(OrderRequest request);
	
	public OrderResponse updatePassengers(OrderRequest request);
	
	public OrderResponse get(String id);
	
	public OrderResponse getTicket(String ticketId);
	
	public OrderResponse book(String id);
	
	public OrderResponse pay(String id);
	
	public OrderResponse cancel(String id);
	
	public OrderResponse returnTickets(OrderRequest request);
	
	public OrderResponse getPdfTickets(OrderRequest request);

}
