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
