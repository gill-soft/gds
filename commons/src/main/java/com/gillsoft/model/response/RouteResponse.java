package com.gillsoft.model.response;

import com.gillsoft.model.Route;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The response which contains the route of trip")
public class RouteResponse extends Response {

	private static final long serialVersionUID = -4197292225196588338L;
	
	@ApiModelProperty(value = "The route of trip", allowEmptyValue = true)
	private Route route;
	
	public RouteResponse() {
		
	}

	public RouteResponse(String id, Route route) {
		setId(id);
		this.route = route;
	}
	
	public RouteResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}
	
}
