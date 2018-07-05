package com.gillsoft.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The vehicle object.")
public class Vehicle implements Serializable {

	private static final long serialVersionUID = -7398284692999348930L;

	@ApiModelProperty(value = "Vehicle id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Vehicle model", allowEmptyValue = true)
	private String model;
	
	@ApiModelProperty(value = "Vehicle state number", allowEmptyValue = true)
	private String number;
	
	@ApiModelProperty(value = "Vehicle capacity", allowEmptyValue = true)
	private Integer capacity;
	
	@ApiModelProperty(value = "The scheme of seats in this vehicle", allowEmptyValue = true)
	private SeatsScheme seatsScheme;

	public Vehicle() {
		
	}

	public Vehicle(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public SeatsScheme getSeatsScheme() {
		return seatsScheme;
	}

	public void setSeatsScheme(SeatsScheme seatsScheme) {
		this.seatsScheme = seatsScheme;
	}

}
