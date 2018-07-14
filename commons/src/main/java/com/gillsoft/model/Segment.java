package com.gillsoft.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "This object describes the trip's segment part.")
public class Segment implements Serializable {

	private static final long serialVersionUID = -6523720730694351564L;

	@ApiModelProperty("Segment id")
	private String id;
	
	@ApiModelProperty(value = "Segment number", allowEmptyValue = true)
	private String number;
	
	@ApiModelProperty(value = "Trip type", allowEmptyValue = true)
	private TripType type;
	
	@ApiModelProperty(value = "Trip route", allowEmptyValue = true)
	private Route route;
	
	@ApiModelProperty(value = "The vehicle which is used on this segment", allowEmptyValue = true)
	private Vehicle vehicle;
	
	@ApiModelProperty(value = "The insurence company", allowEmptyValue = true)
	private Organisation insurance;
	
	@ApiModelProperty(value = "The carrier company", allowEmptyValue = true)
	private Organisation carrier;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	@ApiModelProperty("The departure datetime in format yyyy-MM-dd HH:mm")
	private Date departureDate;

	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	@ApiModelProperty(value = "The arrival datetime in format yyyy-MM-dd HH:mm", allowEmptyValue = true)
	private Date arrivalDate;

	@ApiModelProperty("Departure point")
	private Locality departure;
	
	@ApiModelProperty("Arrival point")
	private Locality arrival;
	
	@ApiModelProperty(value = "Time in way", allowEmptyValue = true)
	private String timeInWay;
	
	@ApiModelProperty(value = "The list of required fields to create order on this segment", allowEmptyValue = true)
	private List<RequiredField> required;
	
	@ApiModelProperty(value = "The count of free seats", allowEmptyValue = true)
	private Integer freeSeatsCount;
	
	@ApiModelProperty(value = "The list of all seats in used vehicle", allowEmptyValue = true)
	private List<Seat> seats;
	
	@ApiModelProperty("The price with tariff description")
	private Price price;
	
	@ApiModelProperty(value = "The map with additional params",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, java.lang.String]")
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

	public List<RequiredField> getRequired() {
		return required;
	}

	public void setRequired(List<RequiredField> required) {
		this.required = required;
	}

	public Integer getFreeSeatsCount() {
		return freeSeatsCount;
	}

	public void setFreeSeatsCount(Integer freeSeatsCount) {
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
