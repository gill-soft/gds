package com.gillsoft.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The object of trip route")
public class Route implements Serializable, Name {

	private static final long serialVersionUID = -1055185265830128083L;

	@ApiModelProperty(value = "Route id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Route number", allowEmptyValue = true)
	private String number;
	
	@ApiModelProperty(value = "Route name on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
	private ConcurrentMap<Lang, String> name;
	
	@ApiModelProperty(value = "Route path", allowEmptyValue = true)
	private List<? extends RoutePoint> path;
	
	@ApiModelProperty("The type of route like as INTERNAL, CITY & etc")
	private RouteType type;
	
	@ApiModelProperty(value = "The map with additional params",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, java.lang.String]")
	private Map<String, String> additionals;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public ConcurrentMap<Lang, String> getName() {
		return name;
	}

	public void setName(ConcurrentMap<Lang, String> name) {
		this.name = name;
	}

	public List<? extends RoutePoint> getPath() {
		return path;
	}

	public void setPath(List<? extends RoutePoint> path) {
		this.path = path;
	}

	public RouteType getType() {
		return type;
	}

	public void setType(RouteType type) {
		this.type = type;
	}

	public Map<String, String> getAdditionals() {
		return additionals;
	}

	public void setAdditionals(Map<String, String> additionals) {
		this.additionals = additionals;
	}

}
