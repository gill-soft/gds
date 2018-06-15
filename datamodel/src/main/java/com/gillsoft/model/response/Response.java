package com.gillsoft.model.response;

import com.gillsoft.model.request.Request;

public abstract class Response extends Request {
	
	private Exception exception;

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
	
}
