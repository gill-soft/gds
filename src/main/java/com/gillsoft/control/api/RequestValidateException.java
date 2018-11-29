package com.gillsoft.control.api;

import com.gillsoft.model.RestError;

public class RequestValidateException extends ApiException {

	private static final long serialVersionUID = -1098046104464720627L;

	public RequestValidateException() {
		super();
	}

	public RequestValidateException(RestError restError) {
		super(restError);
	}

	public RequestValidateException(String message, Throwable cause) {
		super(message, cause);
	}

	public RequestValidateException(String message) {
		super(message);
	}

	public RequestValidateException(Throwable cause) {
		super(cause);
	}

}
