package com.gillsoft.model.request;

import java.util.List;

import com.gillsoft.model.Seat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The request with parameters for receiving the seats, route, tariffs or other details of trip")
public class TripDetailsRequest extends ResourceRequest {
	
	private static final long serialVersionUID = 1282001252361323639L;
	
	@ApiModelProperty(value = "Id of selected trip", required = true)
	private String tripId;
	
	@ApiModelProperty(value = "Id of selected tariff", required = false)
	private String tariffId;
	
	@ApiModelProperty(value = "The list of seat for update", required = false)
	private List<Seat> seats;

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public String getTariffId() {
		return tariffId;
	}

	public void setTariffId(String tariffId) {
		this.tariffId = tariffId;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}
	
}
