package com.gillsoft.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Method {

	public static final String PING = "/api/ping";
	
	public static final String INFO = "/api/info";
	
	public static final String METHOD = "/api/method";
	
	public static final String LOCALITY_ALL = "/api/locality/all";
	
	public static final String LOCALITY_USED = "/api/locality/used";
	
	public static final String LOCALITY_BINDING = "/api/locality/binding";
	
	public static final String SEARCH_INIT = "/api/search/init";
	
	public static final String SEARCH_RESULT = "/api/search/result";
	
	public static final String SEARCH_TRIP = "/api/search/trip";
	
	public static final String SEARCH_TRIP_ROUTE = "/api/search/trip/route";
	
	public static final String SEARCH_TRIP_SEATS_SCHEME = "/api/search/trip/seats/scheme";
	
	public static final String SEARCH_TRIP_SEATS = "/api/search/trip/seats";
	
	public static final String SEARCH_TRIP_SEATS_UPDATE = "/api/search/trip/seats/update";
	
	public static final String SEARCH_TRIP_FARES = "/api/search/trip/fares";
	
	public static final String SEARCH_TRIP_REQUIRED = "/api/search/trip/required";
	
	public static final String SEARCH_TRIP_CONDITIONS = "/api/search/trip/conditions";
	
	public static final String SEARCH_TRIP_DOCUMENTS = "/api/search/trip/documents";
	
	private String name;
	
	private String url;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
