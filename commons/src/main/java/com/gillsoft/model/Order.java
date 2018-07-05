package com.gillsoft.model;

import java.io.Serializable;

public class Order implements Serializable {
	
	private static final long serialVersionUID = -7346969359433791671L;
	
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
