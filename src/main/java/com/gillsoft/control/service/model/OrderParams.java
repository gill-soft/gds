package com.gillsoft.control.service.model;

import java.util.Date;

public class OrderParams {
	
	private long orderId;
	
	private long serviceId;
	
	private Date from;
	
	private Date to;
	
	private int count;
	
	private String resourceNativeOrderId;
	
	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	
	public long getServiceId() {
		return serviceId;
	}

	public void setServiceId(long serviceId) {
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

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getResourceNativeOrderId() {
		return resourceNativeOrderId;
	}

	public void setResourceNativeOrderId(String resourceNativeOrderId) {
		this.resourceNativeOrderId = resourceNativeOrderId;
	}

}
