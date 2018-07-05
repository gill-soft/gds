package com.gillsoft.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The point of route")
public class RoutePoint implements Serializable {

	private static final long serialVersionUID = -6317738337209389045L;

	@ApiModelProperty(value = "Point id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "The departure time from this point", allowEmptyValue = true)
	private String departureTime;
	
	@ApiModelProperty(value = "The arrival time to this point", allowEmptyValue = true)
	private String arrivalTime;
	
	@ApiModelProperty(value = "Platform number", allowEmptyValue = true)
	private String platform;
	
	@ApiModelProperty(value = "The distance from the first point of route", allowEmptyValue = true)
	private Integer distance;
	
	@ApiModelProperty(value = "The number of days from the first point of route", allowEmptyValue = true)
	private Integer arrivalDay;
	
	@ApiModelProperty(value = "The parent locality of this point", allowEmptyValue = true)
	private Locality locality;
	
	public RoutePoint() {
		
	}

	public RoutePoint(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public Integer getArrivalDay() {
		return arrivalDay;
	}

	public void setArrivalDay(Integer arrivalDay) {
		this.arrivalDay = arrivalDay;
	}

	public Locality getLocality() {
		return locality;
	}

	public void setLocality(Locality locality) {
		this.locality = locality;
	}

}
