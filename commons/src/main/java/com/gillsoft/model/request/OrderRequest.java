package com.gillsoft.model.request;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Customer;
import com.gillsoft.model.ServiceItem;

public class OrderRequest extends ResourceRequest {
	
	private String orderId;
	
	private List<ServiceItem> services;
	
	private Map<String, Customer> customers;
	
	private List<OrderRequest> resources;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public List<ServiceItem> getServices() {
		return services;
	}

	public void setServices(List<ServiceItem> services) {
		this.services = services;
	}

	public Map<String, Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(Map<String, Customer> customers) {
		this.customers = customers;
	}

	public List<OrderRequest> getResources() {
		return resources;
	}

	public void setResources(List<OrderRequest> resources) {
		this.resources = resources;
	}
	
}
