package com.gillsoft.model;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Dictionary of api method types")
public enum MethodType {
	
	GET,
	POST,
	PUT,
	PATCH,
	DELETE

}
