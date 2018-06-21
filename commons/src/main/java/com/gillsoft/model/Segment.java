package com.gillsoft.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
	private Vehicle vehicle;
	private Organisation insurance;
	private Organisation carrier;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date departureDate;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	private Date arrivalDate;

	private Locality departure;
	private Locality arrival;
	private String timeInWay;
	private Required required;
	private int freeSeatsCount;
	private List<Seat> seats;
	private Price price;
	private Map<String, String> additionals;

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

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Organisation getInsurance() {
		return insurance;
	}

	public void setInsurance(Organisation insurance) {
		this.insurance = insurance;
	}

	public Organisation getCarrier() {
		return carrier;
	}

	public void setCarrier(Organisation carrier) {
		this.carrier = carrier;
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

	public Locality getDeparture() {
		return departure;
	}

	public void setDeparture(Locality departure) {
		this.departure = departure;
	}

	public Locality getArrival() {
		return arrival;
	}

	public void setArrival(Locality arrival) {
		this.arrival = arrival;
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

	public int getFreeSeatsCount() {
		return freeSeatsCount;
	}

	public void setFreeSeatsCount(int freeSeatsCount) {
		this.freeSeatsCount = freeSeatsCount;
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

	public Map<String, String> getAdditionals() {
		return additionals;
	}

	public void setAdditionals(Map<String, String> additionals) {
		this.additionals = additionals;
	}

}
