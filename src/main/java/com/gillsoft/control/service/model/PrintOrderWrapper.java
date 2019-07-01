package com.gillsoft.control.service.model;

import java.io.Serializable;

import com.gillsoft.model.response.OrderResponse;

public class PrintOrderWrapper implements Serializable {

	private static final long serialVersionUID = 7823001224485059906L;
	
	private OrderResponse order;
	private String ticketLayout;

	public OrderResponse getOrder() {
		return order;
	}

	public void setOrder(OrderResponse order) {
		this.order = order;
	}

	public String getTicketLayout() {
		return ticketLayout;
	}

	public void setTicketLayout(String ticketLayout) {
		this.ticketLayout = ticketLayout;
	}

}
