package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.TripContainer;
import com.gillsoft.model.request.TripSearchRequest;

public class TripSearchResponse extends Response {
	
	private String searchId;
	
	private TripSearchRequest request;
	
	private List<TripContainer> tripContainers;
	
	private List<TripSearchResponse> result;
	
	public TripSearchResponse() {
		
	}

	public TripSearchResponse(String id, String searchId) {
		setId(id);
		this.searchId = searchId;
	}
	
	public TripSearchResponse(String id, String searchId, TripSearchRequest request) {
		setId(id);
		this.searchId = searchId;
		this.request = request;
	}
	
	public TripSearchResponse(String id, String searchId, List<TripContainer> tripContainers, TripSearchRequest request) {
		setId(id);
		this.searchId = searchId;
		this.tripContainers = tripContainers;
		this.request = request;
	}

	public TripSearchResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public TripSearchRequest getRequest() {
		return request;
	}

	public void setRequest(TripSearchRequest request) {
		this.request = request;
	}

	public List<TripContainer> getTripContainers() {
		return tripContainers;
	}

	public void setTripContainers(List<TripContainer> tripContainers) {
		this.tripContainers = tripContainers;
	}

	public List<TripSearchResponse> getResult() {
		return result;
	}

	public void setResult(List<TripSearchResponse> result) {
		this.result = result;
	}

}
