package com.gillsoft.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Route {

	private String id;
	private String number;
	private String name;
	private List<Locality> path;
	private Organisation carrier;
	private RouteType type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Locality> getPath() {
		return path;
	}

	public void setPath(List<Locality> path) {
		this.path = path;
	}

	public Organisation getCarrier() {
		return carrier;
	}

	public void setCarrier(Organisation carrier) {
		this.carrier = carrier;
	}

	public RouteType getType() {
		return type;
	}

	public void setType(RouteType type) {
		this.type = type;
	}

}
