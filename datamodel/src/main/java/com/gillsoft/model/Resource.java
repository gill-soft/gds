package com.gillsoft.model;

import java.rmi.AccessException;

public class Resource {

	
	private AccessException exception;

	public Resource(AccessException exception) {
		this.exception = exception;
	}

	public AccessException getException() {
		return exception;
	}

	public void setException(AccessException exception) {
		this.exception = exception;
	}
	
}
