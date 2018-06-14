package com.gillsoft.model.service;

import com.gillsoft.model.request.ResourceParams;

public interface ResourceService extends ResourceInfoService {
	
	public boolean isAvailable();
	
	public void applayParams(ResourceParams params);
	
	public LocalityService getLocalityService();
	
	public SearchService getSearchService();
	
	public TicketService getTicketService();
	
	public OrderService getOrderService();
	
	public AdditionalService getAdditionalService();

}
