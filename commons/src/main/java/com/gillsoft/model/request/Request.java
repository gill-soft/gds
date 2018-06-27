package com.gillsoft.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public abstract class Request {

	@ApiModelProperty(value = "Current request identifier", required = false, allowEmptyValue = true)
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
