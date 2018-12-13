package com.gillsoft.control.api;

import com.gillsoft.model.RestError;

public class NoDataFoundException extends ApiException {

	private static final long serialVersionUID = -1098046104464720627L;

	public NoDataFoundException() {
		super();
	}

	public NoDataFoundException(RestError restError) {
		super(restError);
	}

	public NoDataFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoDataFoundException(String message) {
		super(message);
	}

	public NoDataFoundException(Throwable cause) {
		super(cause);
	}

}
