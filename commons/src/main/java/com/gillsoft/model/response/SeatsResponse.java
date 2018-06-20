package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.Seat;

public class SeatsResponse extends Response {

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
