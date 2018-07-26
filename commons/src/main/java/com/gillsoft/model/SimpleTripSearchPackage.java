package com.gillsoft.model;

import com.gillsoft.model.request.TripSearchRequest;

public class SimpleTripSearchPackage<T> {
	
	private TripSearchRequest request;
	
	private boolean inProgress;
	
	private T searchResult;
	
	private Exception exception;

	public TripSearchRequest getRequest() {
		return request;
	}

	public void setRequest(TripSearchRequest request) {
		this.request = request;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public T getSearchResult() {
		return searchResult;
	}

	public void setSearchResult(T searchResult) {
		this.searchResult = searchResult;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

}
