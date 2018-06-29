package com.gillsoft.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The service object")
public class ServiceItem {
	
	@ApiModelProperty("The id of created service")
	private String id;
	
	@ApiModelProperty(value = "The return condition id. Applied when service prepared to return.", allowEmptyValue = true)
	private String returnConditionId;
	
	@ApiModelProperty(value = "The attribute that describes the confirmed current service or not", allowEmptyValue = true)
	private Boolean confirmed;
	
	@ApiModelProperty(value = "Service number", allowEmptyValue = true)
	private String number;
	
	@ApiModelProperty("Service customer")
	private Customer customer;
	
	@ApiModelProperty("Service segment")
	private Segment segment;
	
	@ApiModelProperty(value = "The customer seat provided to current service", allowEmptyValue = true)
	private Seat seat;
	
	@ApiModelProperty("Service price")
	private Price price;
	
	@ApiModelProperty(allowEmptyValue = true)
	private RestError error;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReturnConditionId() {
		return returnConditionId;
	}

	public void setReturnConditionId(String returnConditionId) {
		this.returnConditionId = returnConditionId;
	}

	public Boolean getConfirmed() {
		return confirmed;
	}

	public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public Seat getSeat() {
		return seat;
	}

	public void setSeat(Seat seat) {
		this.seat = seat;
	}

	public Price getPrice() {
		return price;
	}

	public void setPrice(Price price) {
		this.price = price;
	}

	public RestError getError() {
		return error;
	}

	public void setError(RestError error) {
		this.error = error;
	}

}
