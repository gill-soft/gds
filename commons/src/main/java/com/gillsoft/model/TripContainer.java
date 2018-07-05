package com.gillsoft.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.request.TripSearchRequest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The container of trips result for single pair of localities and date.")
public class TripContainer implements Serializable {
	
	private static final long serialVersionUID = -1852587707726086196L;

	@ApiModelProperty("The single pair of localities and date on which the trips result was created.")
	private TripSearchRequest request;
	
	@ApiModelProperty(allowEmptyValue = true)
	private RestError error;
	
	@ApiModelProperty(value = "The result list of trips.", allowEmptyValue = true)
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
