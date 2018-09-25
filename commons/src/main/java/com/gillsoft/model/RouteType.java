package com.gillsoft.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The type of route")
public enum RouteType {
	
	INTERNAL, // внутренний
	INTERNATIONAL, // международный
	INTERCITY, // междугородний
	INTERREGIONAL, // междуобласной
	SHUTTLE, // пригородный
	CITY // городской

}
