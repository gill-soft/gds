package com.gillsoft.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Segment {

	private String id;
	private String number;
	private TripType type;
	private Route route;
	private String insuranceId;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date departureDate;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date arrivalDate;

	private String departureId;
	private String arrivalId;
	private String timeInWay;
	private Required required;
	private List<Seat> seats;
	private Price price;

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

	public TripType getType() {
		return type;
	}

	public void setType(TripType type) {
		this.type = type;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public String getInsuranceId() {
		return insuranceId;
	}

	public void setInsuranceId(String insuranceId) {
		this.insuranceId = insuranceId;
	}

	public Date getDepartureDate() {
		return departureDate;
	}

	public void setDepartureDate(Date departureDate) {
		this.departureDate = departureDate;
	}

	public Date getArrivalDate() {
		return arrivalDate;
	}

	public void setArrivalDate(Date arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public String getDepartureId() {
		return departureId;
	}

	public void setDepartureId(String departureId) {
		this.departureId = departureId;
	}

	public String getArrivalId() {
		return arrivalId;
	}

	public void setArrivalId(String arrivalId) {
		this.arrivalId = arrivalId;
	}

	public String getTimeInWay() {
		return timeInWay;
	}

	public void setTimeInWay(String timeInWay) {
		this.timeInWay = timeInWay;
	}

	public Required getRequired() {
		return required;
	}

	public void setRequired(Required required) {
		this.required = required;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}

	public Price getPrice() {
		return price;
	}

	public void setPrice(Price price) {
		this.price = price;
	}

}
