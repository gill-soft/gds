package com.gillsoft.model.request;

import java.util.Objects;

public class ResourceRequest {
	
	private ResourceParams params;
	
	private LocalityRequest localityRequest;
	
	public ResourceParams getParams() {
		return params;
	}

	public void setParams(ResourceParams params) {
		this.params = params;
	}
	
	public LocalityRequest getLocalityRequest() {
		return localityRequest;
	}

	public void setLocalityRequest(LocalityRequest localityRequest) {
		this.localityRequest = localityRequest;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null
				|| !(obj instanceof ResourceRequest)) {
			return false;
		}
		ResourceRequest request = (ResourceRequest) obj;
		return Objects.equals(params, request.getParams());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(params);
	}
	
}
