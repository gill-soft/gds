package com.gillsoft.model;

public class ResponseError extends Exception {

	private static final long serialVersionUID = -486549465922260584L;

	public ResponseError(String message) {
		super(message);
	}

	public ResponseError(String message, Exception nestedException) {
		super(message, nestedException);
	}

}
