package com.gillsoft.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The seats scheme of selected trip")
public class SeatsScheme implements Serializable {

	private static final long serialVersionUID = -8953254717498509027L;
	
	@ApiModelProperty(value = "The map with level as key and list of rows and columns seats ids as value.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.Integer, java.util.List]")
	private Map<Integer, List<List<String>>> scheme;

	public Map<Integer, List<List<String>>> getScheme() {
		return scheme;
	}

	public void setScheme(Map<Integer, List<List<String>>> scheme) {
		this.scheme = scheme;
	}

}
