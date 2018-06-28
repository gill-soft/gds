package com.gillsoft.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The enabled statuses of seats")
public enum SeatStatus {
	
	FREE,
	SALED,
	EMPTY,
	LOCKED

}
