package com.gillsoft.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.request.TripSearchRequest;

@JsonInclude(Include.NON_NULL)
public class TripContainer {
	
	private TripSearchRequest request;
	
	private Error error;
	
	private Map<String, Organisation> organisations;
	
	private Map<String, Locality> localities;
	
	private Map<String, Vehicle> vehicles;
	
	private Map<String, Segment> segments;
	
	private List<Trip> trips;

	public TripSearchRequest getRequest() {
		return request;
	}

	public void setRequest(TripSearchRequest request) {
		this.request = request;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public Map<String, Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(Map<String, Organisation> organisations) {
		this.organisations = organisations;
	}

	public Map<String, Locality> getLocalities() {
		return localities;
	}

	public void setLocalities(Map<String, Locality> localities) {
		this.localities = localities;
	}

	public Map<String, Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Map<String, Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Map<String, Segment> getSegments() {
		return segments;
	}

	public void setSegments(Map<String, Segment> segments) {
		this.segments = segments;
	}

	public List<Trip> getTrips() {
		return trips;
	}

	public void setTrips(List<Trip> trips) {
		this.trips = trips;
	}

}
