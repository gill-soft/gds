package com.gillsoft.control.service.model;

import java.util.Date;
import java.util.List;

import com.gillsoft.model.ServiceStatus;

public class OrderParams {
	
	private Long orderId;
	
	private Long serviceId;
	
	private Date from;
	
	private Date to;
	
	private Integer count;
	
	private String resourceNativeOrderId;
	
	private Boolean reported;
	
	private Date departureFrom;
	
	private Date departureTo;
	
	private Long userId;
	
	private Long clientId;
	
	private String clientPhone;
	
	private List<ServiceStatus> statuses;
	
	private Boolean mappedTrip;
	
	private Date mappedDeparture;

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getResourceNativeOrderId() {
		return resourceNativeOrderId;
	}

	public void setResourceNativeOrderId(String resourceNativeOrderId) {
		this.resourceNativeOrderId = resourceNativeOrderId;
	}

	public Boolean getReported() {
		return reported;
	}

	public void setReported(Boolean reported) {
		this.reported = reported;
	}

	public Date getDepartureFrom() {
		return departureFrom;
	}

	public void setDepartureFrom(Date departureFrom) {
		this.departureFrom = departureFrom;
	}

	public Date getDepartureTo() {
		return departureTo;
	}

	public void setDepartureTo(Date departureTo) {
		this.departureTo = departureTo;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getClientPhone() {
		return clientPhone;
	}

	public void setClientPhone(String clientPhone) {
		this.clientPhone = OrderClient.preparePhone(clientPhone);
	}

	public List<ServiceStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(List<ServiceStatus> statuses) {
		this.statuses = statuses;
	}

	public Boolean getMappedTrip() {
		return mappedTrip;
	}

	public void setMappedTrip(Boolean mappedTrip) {
		this.mappedTrip = mappedTrip;
	}

	public Date getMappedDeparture() {
		return mappedDeparture;
	}

	public void setMappedDeparture(Date mappedDeparture) {
		this.mappedDeparture = mappedDeparture;
	}
	
}
