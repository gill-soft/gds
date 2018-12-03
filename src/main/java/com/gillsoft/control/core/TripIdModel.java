package com.gillsoft.control.core;

import com.gillsoft.model.request.TripSearchRequest;

public class TripIdModel extends IdModel {

	private static final long serialVersionUID = 651911356161511195L;
	
	private TripSearchRequest request;
	
	public TripIdModel() {
		super();
	}

	public TripIdModel(long resourceId, String id, TripSearchRequest request) {
		super(resourceId, id);
		this.request = request;
	}

	public TripSearchRequest getRequest() {
		return request;
	}

	public void setRequest(TripSearchRequest request) {
		this.request = request;
	}
	
	@Override
	public TripIdModel create(String json) {
		return (TripIdModel) super.create(json);
	}

}
