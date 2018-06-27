package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.Method;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response contaions information about available api methods of resource")
public class ResourceMethodResponse extends Response {
	
	@ApiModelProperty("Available api methods")
	private List<Method> methods;
	
	public ResourceMethodResponse(String id, List<Method> methods) {
		setId(id);
		this.methods = methods;
	}
	
	public ResourceMethodResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void setMethods(List<Method> methods) {
		this.methods = methods;
	}
	
}
