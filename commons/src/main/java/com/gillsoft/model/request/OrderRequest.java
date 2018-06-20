package com.gillsoft.model.request;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Customer;
import com.gillsoft.model.Ticket;

public class OrderRequest extends ResourceRequest {
	
	private List<Ticket> tickets;
	
	private Map<String, Customer> customers;
	
	private List<OrderRequest> resources;

	public List<Ticket> getTickets() {
		return tickets;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
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
