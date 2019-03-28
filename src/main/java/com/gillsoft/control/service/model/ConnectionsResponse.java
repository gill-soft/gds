package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ConnectionsResponse implements Serializable {
	
	private static final long serialVersionUID = 4131074144203317000L;

	private Set<Pair> pairs;
	
	private List<SegmentConnection> connections;
	
	private List<List<Long>> routes;

	public Set<Pair> getPairs() {
		return pairs;
	}

	public void setPairs(Set<Pair> pairs) {
		this.pairs = pairs;
	}

	public List<SegmentConnection> getConnections() {
		return connections;
	}

	public void setConnections(List<SegmentConnection> connections) {
		this.connections = connections;
	}

	public List<List<Long>> getRoutes() {
		return routes;
	}

	public void setRoutes(List<List<Long>> routes) {
		this.routes = routes;
	}

}
