package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.Method;

public class ResourceMethodResponse extends Response {
	
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
