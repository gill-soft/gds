package com.gillsoft.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The names of required field")
public enum RequiredField {
	
	NAME,
	PATRONYMIC,
	SURNAME,
	EMAIL,
	PHONE,
	GENDER,
	CITIZENSHIP,
	DOCUMENT_TYPE,
	DOCUMENT_NUMBER,
	DOCUMENT_SERIES,
	BIRTHDAY

}
