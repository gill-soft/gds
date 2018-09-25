package com.gillsoft.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_EMPTY)
@ApiModel(description = "The object of schedule route")
public class ScheduleRoute extends Route {

	private static final long serialVersionUID = -6189535005849623910L;

	@ApiModelProperty(value = "The type of route like as INTERNAL, CITY & etc", allowEmptyValue = true)
	private RouteType type;
	
	@ApiModelProperty(value = "The kind of route like as REGULAR, ADDITIONAL & etc", allowEmptyValue = true)
	private RouteKind kind;
	
	@ApiModelProperty(value = "Route carrier", allowEmptyValue = true)
	private Organisation carrier;
	
	@ApiModelProperty(value = "Route insurance", allowEmptyValue = true)
	private Organisation insurance;
	
	@ApiModelProperty(value = "Route vehicle", allowEmptyValue = true)
	private Vehicle vehicle;
	
	@ApiModelProperty(value = "The date when route started to operate", allowEmptyValue = true)
	private Date startedAt;
	
	@ApiModelProperty(value = "The date when route ended to operate", allowEmptyValue = true)
	private Date endedAt;
	
	@ApiModelProperty(value = "The regularity of route like as EVERY_DAY, DAYS_OF_THE_WEEK & etc", allowEmptyValue = true)
	private Regularity regularity;
	
	@ApiModelProperty(value = "The list of regularity days numbers", allowEmptyValue = true)
	private List<Integer> regularityDays;
	
	@ApiModelProperty(value = "The currency of tariff grid", allowEmptyValue = true)
	private String currency;

	public RouteType getType() {
		return type;
	}

	public void setType(RouteType type) {
		this.type = type;
	}

	public RouteKind getKind() {
		return kind;
	}

	public void setKind(RouteKind kind) {
		this.kind = kind;
	}

	public Organisation getCarrier() {
		return carrier;
	}

	public void setCarrier(Organisation carrier) {
		this.carrier = carrier;
	}

	public Organisation getInsurance() {
		return insurance;
	}

	public void setInsurance(Organisation insurance) {
		this.insurance = insurance;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	public Date getEndedAt() {
		return endedAt;
	}

	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}

	public Regularity getRegularity() {
		return regularity;
	}

	public void setRegularity(Regularity regularity) {
		this.regularity = regularity;
	}

	public List<Integer> getRegularityDays() {
		return regularityDays;
	}

	public void setRegularityDays(List<Integer> regularityDays) {
		this.regularityDays = regularityDays;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}
