package com.gillsoft.model;

public class Method {

	public static final String PING = "/api/ping";
	
	public static final String INFO = "/api/info";
	
	public static final String METHOD = "/api/method";
	
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
