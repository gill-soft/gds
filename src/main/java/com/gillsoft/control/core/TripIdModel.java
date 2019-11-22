package com.gillsoft.control.core;

import java.util.Set;

import com.gillsoft.model.request.TripSearchRequest;

public class TripIdModel extends IdModel {

	private static final long serialVersionUID = 651911356161511195L;
	
	private TripSearchRequest request;
	
	private Set<String> next;
	
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
	
	public Set<String> getNext() {
		return next;
	}

	public void setNext(Set<String> next) {
		this.next = next;
	}

	@Override
	public TripIdModel create(String json) {
		return (TripIdModel) super.create(json);
	}

}
