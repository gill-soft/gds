package com.gillsoft.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The type of route kind")
public enum RouteKind {
	
	REGULAR, // регулярный
	IRREGULAR, // нерегулярный
	ADDITIONAL, // дополнительный
	CHARTERED // чартерный

}
