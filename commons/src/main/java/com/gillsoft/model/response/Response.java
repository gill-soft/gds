package com.gillsoft.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.request.Request;
import com.gillsoft.model.Exception;

@JsonInclude(Include.NON_NULL)
public abstract class Response extends Request {
	
	private Exception exception;

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
	
	public void setException(java.lang.Exception exception) {
		this.exception = new Exception();
		this.exception.setMessage(exception.getMessage());
		if (exception.getCause() != null) {
			this.exception.setCause(exception.getCause().getMessage());
		}
	}
	
}
