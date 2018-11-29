package com.gillsoft.control.api;

import com.gillsoft.model.RestError;

public class MethodUnavalaibleException extends ApiException {

	private static final long serialVersionUID = -9101253797750210981L;

	public MethodUnavalaibleException() {
		super();
	}

	public MethodUnavalaibleException(RestError restError) {
		super(restError);
	}

	public MethodUnavalaibleException(String message, Throwable cause) {
		super(message, cause);
	}

	public MethodUnavalaibleException(String message) {
		super(message);
	}

	public MethodUnavalaibleException(Throwable cause) {
		super(cause);
	}

}
