package com.gillsoft.model.service;

import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

public interface OrderService {
	
	public OrderResponse create(OrderRequest request);
	
	public OrderResponse addServices(OrderRequest request);
	
	public OrderResponse removeServices(OrderRequest request);
	
	public OrderResponse updateCustomers(OrderRequest request);
	
	public OrderResponse get(String orderId);
	
	public OrderResponse getService(String ticketId);
	
	public OrderResponse booking(String orderId);
	
	public OrderResponse confirm(String orderId);
	
	public OrderResponse cancel(String orderId);
	
	public OrderResponse prepareReturnServices(OrderRequest request);
	
	public OrderResponse returnServices(OrderRequest request);
	
	public OrderResponse getPdfDocuments(OrderRequest request);

}
