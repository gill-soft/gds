package com.gillsoft.service;

import java.util.List;

import com.gillsoft.model.Method;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceParams;

public interface ResourceService {
	
	public void applayParams(ResourceParams params);
	
	public boolean isAvailable();
	
	public Resource getInfo();
	
	public List<Method> getAvailableMethods();
	
	public LocalityService getLocationService();
	
	public SearchService getSearchService();
	
	public TicketService getTicketService();
	
	public OrderService getOrderService();
	
	public AdditionalService getAdditionalService();

}
