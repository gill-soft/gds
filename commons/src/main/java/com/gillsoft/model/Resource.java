package com.gillsoft.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "Resource information")
public class Resource implements Serializable {

	private static final long serialVersionUID = -8794588643510471510L;

	@ApiModelProperty("Uniq resource code")
	private String code;
	
	@ApiModelProperty("Resource shot name or trade mark")
	private String name;
	
	@ApiModelProperty(value = "Information about resource", allowEmptyValue = true)
	private String description;
	
	public Resource() {
		
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
