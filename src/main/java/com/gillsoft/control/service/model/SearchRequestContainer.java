package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;

public class SearchRequestContainer implements Serializable {

	private static final long serialVersionUID = -5342988125483181617L;

	private TripSearchRequest originRequest;
	
	private Map<String, List<TripSearchRequest>> pairRequests;
	
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
		return pairRequests == null || pairRequests.isEmpty();
	}
	
	public void setSearchPair(TripSearchRequest request) {
		for (Entry<String, List<TripSearchRequest>> requests : pairRequests.entrySet()) {
			for (TripSearchRequest searchRequest : requests.getValue()) {
				if (searchRequest.getId().equals(request.getId())) {
					request.setLocalityPairs(Collections.singletonList(requests.getKey().split(";")));
					return;
				}
			}
		}
	}
	
	public List<TripSearchRequest> getRequests() {
		return pairRequests.values().stream().flatMap(List::stream).collect(Collectors.toList());
	}
	
	public void add(String pairKey, TripSearchRequest request) {
		if (pairRequests == null) {
			pairRequests = new HashMap<>();
		}
		if (!pairRequests.containsKey(pairKey)) {
			pairRequests.put(pairKey, new ArrayList<>());
		}
		pairRequests.get(pairKey).add(request);
	}

	public Map<String, List<TripSearchRequest>> getPairRequests() {
		return pairRequests;
	}

	public void setPairRequests(Map<String, List<TripSearchRequest>> pairRequests) {
		this.pairRequests = pairRequests;
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
