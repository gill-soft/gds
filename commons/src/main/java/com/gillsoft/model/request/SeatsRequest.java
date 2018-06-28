package com.gillsoft.model.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The request with parameters for receiving the seats of trip")
public class SeatsRequest extends ResourceRequest {
	
	@ApiModelProperty(value = "Id of selected trip", required = true)
	private String tripId;

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
	
}
