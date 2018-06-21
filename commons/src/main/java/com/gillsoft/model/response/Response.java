package com.gillsoft.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.request.Request;
import com.gillsoft.model.RestError;

@JsonInclude(Include.NON_NULL)
public abstract class Response extends Request {
	
	private RestError error;

	public RestError getError() {
		return error;
	}

	public void setError(RestError error) {
		this.error = error;
	}

	public void setException(java.lang.Exception exception) {
		this.error = new RestError();
		this.error.setError(exception.getMessage());
		if (exception.getCause() != null) {
			this.error.setMessage(exception.getCause().getMessage());
		}
	}
	
}
