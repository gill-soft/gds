package com.gillsoft.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The enabled types of seats")
public enum SeatType {

	SEAT,
	FLOOR,
	EXIT,
	DRIVER
	
}
