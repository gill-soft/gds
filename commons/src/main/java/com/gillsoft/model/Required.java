package com.gillsoft.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The list of required fields")
public class Required {
	
	@ApiModelProperty(value = "The map contains order field and boolean property",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, java.lang.Boolean]")
	private Map<String, Boolean> fields;

	public Map<String, Boolean> getFields() {
		return fields;
	}

	public void setFields(Map<String, Boolean> fields) {
		this.fields = fields;
	}
	
}
