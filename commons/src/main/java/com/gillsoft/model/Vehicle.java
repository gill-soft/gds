package com.gillsoft.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The vehicle object.")
public class Vehicle {

	@ApiModelProperty("Vehicle id")
	private String id;
	
	@ApiModelProperty("Vehicle model")
	private String model;
	
	@ApiModelProperty("Vehicle state number")
	private String number;
	
	@ApiModelProperty("Vehicle capacity")
	private Integer capacity;
	
	@ApiModelProperty("The scheme of seats in this vehicle")
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
