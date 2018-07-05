package com.gillsoft.model.response;

import com.gillsoft.model.Resource;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Response contains information about resource")
public class ResourceResponse extends Response {
	
	private static final long serialVersionUID = -2874846502816116180L;
	
	private Resource resource;
	
	public ResourceResponse(String id, Resource resource) {
		setId(id);
		this.resource = resource;
	}
	
	public ResourceResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
}
