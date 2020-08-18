package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gillsoft.model.ApiDateDeserializer;
import com.gillsoft.model.ApiDateSerializer;

public class TripDateServices implements Serializable {
	
	private static final long serialVersionUID = -1495637524944058601L;

	private long tripId;
	
	@JsonSerialize(using = ApiDateSerializer.class)
	@JsonDeserialize(using = ApiDateDeserializer.class)
	private Date departure;
	
	private long count;

	public TripDateServices() {
		
	}

	public TripDateServices(long tripId, Date departure, long count) {
		this.tripId = tripId;
		this.departure = departure;
		this.count = count;
	}

	public long getTripId() {
		return tripId;
	}

	public void setTripId(long tripId) {
		this.tripId = tripId;
	}

	public Date getDeparture() {
		return departure;
	}

	public void setDeparture(Date departure) {
		this.departure = departure;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

}
