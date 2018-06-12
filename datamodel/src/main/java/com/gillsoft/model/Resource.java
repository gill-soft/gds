package com.gillsoft.model;

public class Resource {

	private String code;
	private String name;
	private String description;
	
	private Exception exception;

	public Resource() {
		
	}

	public Resource(Exception exception) {
		this.exception = exception;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
	
}
