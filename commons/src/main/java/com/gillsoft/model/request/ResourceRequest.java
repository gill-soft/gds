package com.gillsoft.model.request;

import java.util.Objects;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The request with resource parameters")
public class ResourceRequest extends Request {
	
	private static final long serialVersionUID = -6941566879593289537L;
	
	private ResourceParams params;
	
	public ResourceParams getParams() {
		return params;
	}

	public void setParams(ResourceParams params) {
		this.params = params;
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
