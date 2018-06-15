package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.Trip;
import com.gillsoft.model.request.TripSearchRequest;

public class TripSearchResponse extends Response {
	
	private String searchId;
	
	private TripSearchRequest request;
	
	private List<Trip> trips;
	
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
	
	public TripSearchResponse(String id, String searchId, List<Trip> trips, TripSearchRequest request) {
		setId(id);
		this.searchId = searchId;
		this.trips = trips;
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

	public List<Trip> getTrips() {
		return trips;
	}

	public void setTrips(List<Trip> trips) {
		this.trips = trips;
	}

	public List<TripSearchResponse> getResult() {
		return result;
	}

	public void setResult(List<TripSearchResponse> result) {
		this.result = result;
	}

}
