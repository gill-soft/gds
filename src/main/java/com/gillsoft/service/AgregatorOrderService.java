package com.gillsoft.service;

import java.util.List;

import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

public interface AgregatorOrderService {
	
	public OrderResponse create(OrderRequest request);
	
	public List<OrderResponse> addServices(List<OrderRequest> request);
	
	public List<OrderResponse> removeServices(List<OrderRequest> request);
	
	public List<OrderResponse> updateCustomers(List<OrderRequest> request);
	
	public List<OrderResponse> get(List<OrderRequest> request);
	
	public List<OrderResponse> getService(List<OrderRequest> request);
	
	public List<OrderResponse> booking(List<OrderRequest> request);
	
	public List<OrderResponse> confirm(List<OrderRequest> request);
	
	public List<OrderResponse> cancel(List<OrderRequest> request);
	
	public List<OrderResponse> prepareReturnServices(List<OrderRequest> request);
	
	public List<OrderResponse> returnServices(List<OrderRequest> request);
	
	public List<OrderResponse> getPdfDocuments(List<OrderRequest> request);

}
