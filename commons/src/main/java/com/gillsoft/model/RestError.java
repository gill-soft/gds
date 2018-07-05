package com.gillsoft.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The error which thrown during execute request")
public class RestError implements Serializable {

	private static final long serialVersionUID = -8593702101013641125L;

	@JsonFormat(shape=Shape.STRING)
	@ApiModelProperty("The time when error was generated")
	private Date time = new Date();
	
	@ApiModelProperty(value = "Error name", allowEmptyValue = true)
	private String name;
	
	@ApiModelProperty(value = "Error message", allowEmptyValue = true)
	private String message;
	
	public RestError() {
		
	}

	public RestError(String message) {
		this.message = message;
	}

	public RestError(String name, String message) {
		this.name = name;
		this.message = message;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String error) {
		this.name = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
