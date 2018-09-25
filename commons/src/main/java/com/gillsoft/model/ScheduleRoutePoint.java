package com.gillsoft.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.Price;
import com.gillsoft.model.RoutePoint;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The object of schedule route point")
public class ScheduleRoutePoint extends RoutePoint {

	private static final long serialVersionUID = 8288314165752053262L;

	@ApiModelProperty("Index of route path point")
	private int index;
	
	@ApiModelProperty(value = "The list of destination points with tariff price", allowEmptyValue = true)
	private List<ScheduleRoutePoint> destinations;
	
	@ApiModelProperty(value = "The tariff price for current point of route", allowEmptyValue = true)
	private Price price;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<ScheduleRoutePoint> getDestinations() {
		return destinations;
	}

	public void setDestinations(List<ScheduleRoutePoint> destinations) {
		this.destinations = destinations;
	}

	public Price getPrice() {
		return price;
	}

	public void setPrice(Price price) {
		this.price = price;
	} 

}
