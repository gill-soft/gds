package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.Seat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The response which contains the list of trips seats")
public class SeatsResponse extends Response {

	private static final long serialVersionUID = 1952714650062579123L;
	
	@ApiModelProperty(value = "The list of trips seats", allowEmptyValue = true)
	private List<Seat> seats;
	
	public SeatsResponse(String id, List<Seat> seats) {
		setId(id);
		this.seats = seats;
	}
	
	public SeatsResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}
	
}
