package com.gillsoft.control.api;

import com.gillsoft.model.RestError;

public class OperationLockedException extends ApiException {

	private static final long serialVersionUID = 3013795108834407309L;

	public OperationLockedException() {
		super();
	}

	public OperationLockedException(RestError restError) {
		super(restError);
	}

	public OperationLockedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OperationLockedException(String message) {
		super(message);
	}

	public OperationLockedException(Throwable cause) {
		super(cause);
	}
	
}
