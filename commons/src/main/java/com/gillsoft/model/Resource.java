package com.gillsoft.model;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "Resource information")
public class Resource implements Serializable, Name, Description {

	private static final long serialVersionUID = -8794588643510471510L;

	@ApiModelProperty("Uniq resource code")
	private String code;
	
	@ApiModelProperty(value = "Resource shot name or trade mark on a different language",
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
	private ConcurrentMap<Lang, String> name;
	
	@ApiModelProperty(value = "Information about resource on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
	private ConcurrentMap<Lang, String> description;
	
	public Resource() {
		
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public ConcurrentMap<Lang, String> getName() {
		return name;
	}

	public void setName(ConcurrentMap<Lang, String> name) {
		this.name = name;
	}

	public ConcurrentMap<Lang, String> getDescription() {
		return description;
	}

	public void setDescription(ConcurrentMap<Lang, String> description) {
		this.description = description;
	}

}
