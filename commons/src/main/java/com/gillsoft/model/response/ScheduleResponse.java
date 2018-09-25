package com.gillsoft.model.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.ScheduleRoute;
import com.gillsoft.model.Vehicle;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The object of schedule")
public class ScheduleResponse extends Response {
	
	private static final long serialVersionUID = -4352974802028963051L;

	@ApiModelProperty(value = "The map of used organisations in this schedule.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Organisation]")
	private Map<String, Organisation> organisations;

	@ApiModelProperty(value = "The map of used localities in this schedule.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Locality]")
	private Map<String, Locality> localities;

	@ApiModelProperty(value = "The map of used parent localities in this schedule.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Locality]")
	private Map<String, Locality> parents;
	
	@ApiModelProperty(value = "The map of used vehicles in this schedule.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Vehicle]")
	private Map<String, Vehicle> vehicles;
	
	@ApiModelProperty("The list of routes.")
	private List<ScheduleRoute> routes;
	
	public ScheduleResponse() {
		
	}

	public ScheduleResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public Map<String, Organisation> getOrganisations() {
		return organisations;
	}

	public void setOrganisations(Map<String, Organisation> organisations) {
		this.organisations = organisations;
	}

	public Map<String, Locality> getLocalities() {
		return localities;
	}

	public void setLocalities(Map<String, Locality> localities) {
		this.localities = localities;
	}

	public Map<String, Locality> getParents() {
		return parents;
	}

	public void setParents(Map<String, Locality> parents) {
		this.parents = parents;
	}

	public Map<String, Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Map<String, Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public List<ScheduleRoute> getRoutes() {
		return routes;
	}

	public void setRoutes(List<ScheduleRoute> routes) {
		this.routes = routes;
	}

}
