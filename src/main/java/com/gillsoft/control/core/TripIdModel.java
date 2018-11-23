package com.gillsoft.control.core;

import com.gillsoft.model.AbstractJsonModel;

public class TripIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 651911356161511195L;
	
	private long resourceId;
	
	private String tripId;

	public TripIdModel() {
		
	}

	public TripIdModel(long resourceId, String tripId) {
		this.resourceId = resourceId;
		this.tripId = tripId;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	@Override
	public TripIdModel create(String json) {
		return (TripIdModel) super.create(json);
	}
	
}
