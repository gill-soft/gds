package com.gillsoft.model.request;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Customer;
import com.gillsoft.model.ServiceItem;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The request to create order")
public class OrderRequest extends ResourceRequest {
	
	private static final long serialVersionUID = -6474441309347766267L;

	@ApiModelProperty(value = "Request order id", required = true)
	private String orderId;
	
	@ApiModelProperty(value = "The list of selected services", required = true)
	private List<ServiceItem> services;
	
	@ApiModelProperty(value = "The map of order customers", required = true,
			dataType="java.util.Map[java.lang.String, com.gillsoft.model.Customer]")
	private Map<String, Customer> customers;
	
	@ApiModelProperty(value = "The list of requests to resources", required = true)
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
