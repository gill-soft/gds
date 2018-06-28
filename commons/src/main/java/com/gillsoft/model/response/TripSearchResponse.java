package com.gillsoft.model.response;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Segment;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The response to the trips search.")
public class TripSearchResponse extends Response {
	
	@ApiModelProperty(value = "The search id. It will be used for receiving the next search result. If id is empty, than the current search is completed.",
			allowEmptyValue = true)
	private String searchId;
	
	@ApiModelProperty(hidden = true)
	private TripSearchRequest request;
	
	@ApiModelProperty(value = "The map of used organisations in this search result part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Organisation]")
	private Map<String, Organisation> organisations;
	
	@ApiModelProperty(value = "The map of used localities in this search result part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Locality]")
	private Map<String, Locality> localities;
	
	@ApiModelProperty(value = "The map of used vehicles in this search result part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Vehicle]")
	private Map<String, Vehicle> vehicles;
	
	@ApiModelProperty(value = "The map of used trip segments in this search result part.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, com.gillsoft.model.Segment]")
	private Map<String, Segment> segments;
	
	@ApiModelProperty("The list of container of trips result which contains request and received result by this request.")
	private List<TripContainer> tripContainers;
	
	@ApiModelProperty("The list of responses to the each trips search by resources.")
	private List<TripSearchResponse> result;
	
	public TripSearchResponse() {
		
	}

	public TripSearchResponse(String id, String searchId) {
		setId(id);
		this.searchId = searchId;
	}
	
	public TripSearchResponse(String id, String searchId, TripSearchRequest request) {
		setId(id);
		this.searchId = searchId;
		this.request = request;
	}
	
	public TripSearchResponse(String id, String searchId, List<TripContainer> tripContainers, TripSearchRequest request) {
		setId(id);
		this.searchId = searchId;
		this.tripContainers = tripContainers;
		this.request = request;
	}

	public TripSearchResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public TripSearchRequest getRequest() {
		return request;
	}

	public void setRequest(TripSearchRequest request) {
		this.request = request;
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

	public Map<String, Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(Map<String, Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Map<String, Segment> getSegments() {
		return segments;
	}

	public void setSegments(Map<String, Segment> segments) {
		this.segments = segments;
	}

	public List<TripContainer> getTripContainers() {
		return tripContainers;
	}

	public void setTripContainers(List<TripContainer> tripContainers) {
		this.tripContainers = tripContainers;
	}

	public List<TripSearchResponse> getResult() {
		return result;
	}

	public void setResult(List<TripSearchResponse> result) {
		this.result = result;
	}

}
