package com.gillsoft.model.response;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.gillsoft.model.Customer;
import com.gillsoft.model.Document;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.Vehicle;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The response that describes the order")
public class OrderResponse extends Response {
	
	private static final long serialVersionUID = -1166178114134098429L;

	@ApiModelProperty(value = "Order id", required = true)
	private String orderId;

	@ApiModelProperty(value = "The map of used organisations in this order part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Organisation]")
	private Map<String, Organisation> organisations;

	@ApiModelProperty(value = "The map of used localities in this order part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Locality]")
	private Map<String, Locality> localities;

	@ApiModelProperty(value = "The map of used vehicles in this order part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Vehicle]")
	private Map<String, Vehicle> vehicles;

	@ApiModelProperty(value = "The map of used trip segments in this order part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Segment]")
	private Map<String, Segment> segments;

	@ApiModelProperty(value = "The map of used customers in this order part.",
			dataType="java.util.Map[java.lang.String, com.gillsoft.model.Customer]")
	private Map<String, Customer> customers;

	@ApiModelProperty(value = "The list of created services", required = true)
	private List<ServiceItem> services;

	@ApiModelProperty("The list that contains orders of resource")
	private List<OrderResponse> resources;

	@ApiModelProperty(value = "The map with additional params",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, java.lang.String]")
	private Map<String, String> additionals;
	
	@ApiModelProperty("The list that contains documents of current order")
	private List<Document> documents;
	
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

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public void fillMaps() {
		if (services != null) {
			for (ServiceItem service : services) {
				if (service.getSegment() != null) {
					// organisations
					if (service.getSegment().getInsurance() != null
							&& service.getSegment().getInsurance().getId() != null) {
						if (organisations == null || organisations.isEmpty()
								|| !organisations.containsKey(service.getSegment().getInsurance().getId())) {
							if (organisations == null) {
								organisations = new HashMap<>();
							}
							organisations.put(service.getSegment().getInsurance().getId(),
									service.getSegment().getInsurance());
						}
						service.getSegment()
								.setInsurance(new Organisation(service.getSegment().getInsurance().getId()));
					}
					if (service.getSegment().getCarrier() != null
							&& service.getSegment().getCarrier().getId() != null) {
						if (organisations == null || organisations.isEmpty()
								|| !organisations.containsKey(service.getSegment().getCarrier().getId())) {
							if (organisations == null) {
								organisations = new HashMap<>();
							}
							organisations.put(service.getSegment().getCarrier().getId(),
									service.getSegment().getCarrier());
						}
						service.getSegment().setCarrier(new Organisation(service.getSegment().getCarrier().getId()));
					}
					// localities
					if (service.getSegment().getDeparture() != null
							&& service.getSegment().getDeparture().getId() != null) {
						if (localities == null || localities.isEmpty()
								|| !localities.containsKey(service.getSegment().getDeparture().getId())) {
							if (localities == null) {
								localities = new HashMap<>();
							}
							localities.put(service.getSegment().getDeparture().getId(),
									service.getSegment().getDeparture());
						}
						service.getSegment().setDeparture(new Locality(service.getSegment().getDeparture().getId()));
					}
					if (service.getSegment().getArrival() != null
							&& service.getSegment().getArrival().getId() != null) {
						if (localities == null || localities.isEmpty()
								|| !localities.containsKey(service.getSegment().getArrival().getId())) {
							if (localities == null) {
								localities = new HashMap<>();
							}
							localities.put(service.getSegment().getArrival().getId(),
									service.getSegment().getArrival());
						}
						service.getSegment().setArrival(new Locality(service.getSegment().getArrival().getId()));
					}
				}
				// segments
				if (service.getSegment() != null && service.getSegment().getId() != null) {
					if (segments == null || segments.isEmpty() || !segments.containsKey(service.getSegment().getId())) {
						if (segments == null) {
							segments = new HashMap<>();
						}
						segments.put(service.getSegment().getId(), service.getSegment());
					}
					service.setSegment(new Segment(service.getSegment().getId()));
				}
				// customers
				if (service.getCustomer() != null && service.getCustomer().getId() != null) {
					if (customers == null || customers.isEmpty()
							|| !customers.containsKey(service.getCustomer().getId())) {
						if (customers == null) {
							customers = new HashMap<>();
						}
						customers.put(service.getCustomer().getId(), service.getCustomer());
					}
					service.setCustomer(new Customer(service.getCustomer().getId()));
				}
			}
		}
	}

}
