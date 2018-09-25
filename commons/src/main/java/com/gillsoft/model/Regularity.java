package com.gillsoft.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The type of route regularity")
public enum Regularity {

	EVERY_DAY, // каждый день
	DAY_BY_DAY, // через день
	EVEN_DAY, // по чётным дням
	ODD_DAY, // по нечётным дням
	DAYS_OF_THE_WEEK // в выбранные дни недели
	
}
