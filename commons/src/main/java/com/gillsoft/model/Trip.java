package com.gillsoft.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "Generated from a link to id a trip or a round trip or a connecting trip.")
public class Trip {

	@ApiModelProperty(value = "Direct trip segment id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Back trip segment id for roundtrip", allowEmptyValue = true)
	private String backId;
	
	@ApiModelProperty(value = "The list of id of connecting trip", allowEmptyValue = true)
	private List<String> segments;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBackId() {
		return backId;
	}

	public void setBackId(String backId) {
		this.backId = backId;
	}

	public List<String> getSegments() {
		return segments;
	}

	public void setSegments(List<String> segments) {
		this.segments = segments;
	}

}
