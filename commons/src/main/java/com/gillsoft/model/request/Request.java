package com.gillsoft.model.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
public abstract class Request implements Serializable {

	private static final long serialVersionUID = 658531208691977373L;
	
	@ApiModelProperty(value = "Current request identifier", required = false, allowEmptyValue = true)
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
