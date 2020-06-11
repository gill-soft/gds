package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.util.StringUtil;

public class SearchRequestContainer implements Serializable {

	private static final long serialVersionUID = -5342988125483181617L;

	private TripSearchRequest originRequest;
	
	private List<TripSearchRequest> requests;
	
	private Set<String> presentRequests;
	
	private ConnectionsResponse connections;
	
	private Set<String> presentTrips;
	
	private TripSearchResponse response;
	
	public TripSearchRequest getOriginRequest() {
		return originRequest;
	}

	public void setOriginRequest(TripSearchRequest originRequest) {
		this.originRequest = originRequest;
	}
	
	public boolean isEmpty() {
		return requests == null || requests.isEmpty();
	}
	
	public void add(TripSearchRequest request) {
		if (addSearchRequestKeys(request)) {
			if (requests == null) {
				requests = new LinkedList<>();
			}
			requests.add(request);
		}
	}
	
	private boolean addSearchRequestKeys(TripSearchRequest request) {
		boolean added = false;
		for (Date date : request.getDates()) {
			if (request.getBackDates() != null) {
				for (Date back : request.getBackDates()) {
					if (addSearchRequestKeys(date, back, request)) {
						added = true;
					}
				}
			} else {
				if (addSearchRequestKeys(date, null, request)) {
					added = true;
				}
			}
		}
		return added;
	}
	
	private boolean addSearchRequestKeys(Date date, Date back, TripSearchRequest request) {
		boolean added = false;
		for (String[] pair : request.getLocalityPairs()) {
			StringBuilder key = new StringBuilder();
			key.append(StringUtil.dateFormat.format(date)).append(";");
			if (back != null) {
				key.append(StringUtil.dateFormat.format(back)).append(";");
			}
			key.append(String.join(";", pair)).append(";");
			key.append(request.getCurrency()).append(";");
			key.append(request.getLang()).append(";");
			if (request.getParams() != null) {
				if (request.getParams().getResource() != null) {
					key.append(request.getParams().getResource().getId()).append(";");
					key.append(request.getParams().getResource().getCode()).append(";");
				}
				key.append(request.getParams().getHost()).append(";");
			}
			if (presentRequests == null) {
				presentRequests = new HashSet<>();
			}
			if (presentRequests.add(key.toString())) {
				added = true;
			}
		}
		return added;
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
