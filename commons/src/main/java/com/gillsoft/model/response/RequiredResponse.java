package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.RequiredField;

import io.swagger.annotations.ApiModelProperty;

public class RequiredResponse extends Response {

	private static final long serialVersionUID = 4935770131342405606L;
	
	@ApiModelProperty(value = "The route of trip", allowEmptyValue = true)
	private List<RequiredField> fields;
	
	public RequiredResponse(String id, List<RequiredField> fields) {
		setId(id);
		this.fields = fields;
	}
	
	public RequiredResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public List<RequiredField> getFields() {
		return fields;
	}

	public void setFields(List<RequiredField> fields) {
		this.fields = fields;
	}
	
}
