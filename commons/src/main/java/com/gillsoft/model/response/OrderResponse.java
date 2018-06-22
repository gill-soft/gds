package com.gillsoft.model.response;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Customer;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.Vehicle;

public class OrderResponse extends Response {
	
	private String orderId;

	private Map<String, Organisation> organisations;

	private Map<String, Locality> localities;

	private Map<String, Vehicle> vehicles;

	private Map<String, Segment> segments;

	private Map<String, Customer> customers;

	private List<ServiceItem> services;

	private List<OrderResponse> resources;

	private Map<String, String> additionals;
	
	public OrderResponse() {
		
	}

	public OrderResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Map<String, Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(Map<String, Organisation> organisations) {
		this.organisations = organisations;
	}

	public Map<String, Locality> getLocalities() {
		return localities;
	}

	public void setLocalities(Map<String, Locality> localities) {
		this.localities = localities;
	}

	public Map<String, Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Map<String, Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Map<String, Segment> getSegments() {
		return segments;
	}

	public void setSegments(Map<String, Segment> segments) {
		this.segments = segments;
	}

	public Map<String, Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(Map<String, Customer> customers) {
		this.customers = customers;
	}

	public List<ServiceItem> getServices() {
		return services;
	}

	public void setServices(List<ServiceItem> services) {
		this.services = services;
	}

	public List<OrderResponse> getResources() {
		return resources;
	}

	public void setResources(List<OrderResponse> resources) {
		this.resources = resources;
	}

	public Map<String, String> getAdditionals() {
		return additionals;
	}

	public void setAdditionals(Map<String, String> additionals) {
		this.additionals = additionals;
	}

}
