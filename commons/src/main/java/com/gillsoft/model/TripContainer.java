package com.gillsoft.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.request.TripSearchRequest;

@JsonInclude(Include.NON_NULL)
public class TripContainer {
	
	private TripSearchRequest request;
	
	private RestError error;
	
	private List<Trip> trips;

	public TripSearchRequest getRequest() {
		return request;
	}

	public void setRequest(TripSearchRequest request) {
		this.request = request;
	}

	public RestError getError() {
		return error;
	}

	public void setError(RestError error) {
		this.error = error;
	}

	public List<Trip> getTrips() {
		return trips;
	}

	public void setTrips(List<Trip> trips) {
		this.trips = trips;
	}

}
