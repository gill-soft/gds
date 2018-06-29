package com.gillsoft.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The object of trip route")
public class Route {

	@ApiModelProperty(value = "Route id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Route number", allowEmptyValue = true)
	private String number;
	
	@ApiModelProperty(value = "Route name", allowEmptyValue = true)
	private String name;
	
	@ApiModelProperty(value = "Route path", allowEmptyValue = true)
	private List<RoutePoint> path;
	
	@ApiModelProperty("Route type")
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

	public List<RoutePoint> getPath() {
		return path;
	}

	public void setPath(List<RoutePoint> path) {
		this.path = path;
	}

	public RouteType getType() {
		return type;
	}

	public void setType(RouteType type) {
		this.type = type;
	}

}
