package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;

public class SearchRequestContainer implements Serializable {

	private static final long serialVersionUID = -5342988125483181617L;

	private TripSearchRequest originRequest;
	
	private List<TripSearchRequest> requests;
	
	private ConnectionsResponse connections;
	
	private Set<String> presentTrips;
	
	private TripSearchResponse response;
	
	public TripSearchRequest getOriginRequest() {
		return originRequest;
	}

	public void setOriginRequest(TripSearchRequest originRequest) {
		this.originRequest = originRequest;
	}

	public void add(TripSearchRequest request) {
		if (requests == null) {
			requests = new ArrayList<>();
		}
		requests.add(request);
	}
	
	public boolean isEmpty() {
		return requests == null || requests.isEmpty();
	}

	public void setRequests(List<TripSearchRequest> requests) {
		this.requests = requests;
	}

	public List<TripSearchRequest> getRequests() {
		return requests;
	}

	public ConnectionsResponse getConnections() {
		return connections;
	}

	public void setConnections(ConnectionsResponse connections) {
		this.connections = connections;
	}
	
	public void addTrip(String id) {
		if (presentTrips == null) {
			presentTrips = new HashSet<>();
		}
		presentTrips.add(id);
	}
	
	public boolean isPresentTrip(String id) {
		return presentTrips != null
				&& presentTrips.contains(id);
	}

	public Set<String> getPresentTrips() {
		return presentTrips;
	}

	public void setPresentTrips(Set<String> presentTrips) {
		this.presentTrips = presentTrips;
	}

	public TripSearchResponse getResponse() {
		return response;
	}

	public void setResponse(TripSearchResponse response) {
		this.response = response;
	}

}
