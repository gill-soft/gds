package com.gillsoft.control.api;

import com.gillsoft.model.RestError;

public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 8504848312946583403L;
	
	private RestError restError;
	
	public ApiException() {
		super();
	}

	public ApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApiException(String message) {
		super(message);
	}

	public ApiException(Throwable cause) {
		super(cause);
	}

	public ApiException(RestError restError) {
		this.restError = restError;
	}

	public RestError createRestError() {
		return restError != null ? restError :
			new RestError(getMessage(), getCause() != null ? getCause().getMessage() : null);
	}

}
