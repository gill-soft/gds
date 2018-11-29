package com.gillsoft.control.api;

import com.gillsoft.model.RestError;

public class ResourceUnavailableException extends ApiException {

	private static final long serialVersionUID = 3975448177503163918L;

	public ResourceUnavailableException() {
		super();
	}

	public ResourceUnavailableException(RestError restError) {
		super(restError);
	}

	public ResourceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceUnavailableException(String message) {
		super(message);
	}

	public ResourceUnavailableException(Throwable cause) {
		super(cause);
	}

}
