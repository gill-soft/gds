package com.gillsoft.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "Seat object")
public class Seat implements Serializable {

	private static final long serialVersionUID = 826565428053355100L;

	@ApiModelProperty("Seat id")
	private String id;
	
	@ApiModelProperty("Seat type")
	private SeatType type;
	
	@ApiModelProperty("Seat number")
	private String number;
	
	@ApiModelProperty("Seat status")
	private SeatStatus status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SeatType getType() {
		return type;
	}

	public void setType(SeatType type) {
		this.type = type;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public SeatStatus getStatus() {
		return status;
	}

	public void setStatus(SeatStatus status) {
		this.status = status;
	}

}
