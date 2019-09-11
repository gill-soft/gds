package com.gillsoft.control.service.model;

import java.io.Serializable;

import com.gillsoft.model.Lang;
import com.gillsoft.model.response.OrderResponse;

public class PrintOrderWrapper implements Serializable {

	private static final long serialVersionUID = 7823001224485059906L;
	
	private Lang lang;
	private OrderResponse order;
	private String ticketLayout;

	public Lang getLang() {
		return lang;
	}

	public void setLang(Lang lang) {
		this.lang = lang;
	}

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
