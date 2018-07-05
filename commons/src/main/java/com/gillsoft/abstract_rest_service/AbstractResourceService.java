package com.gillsoft.abstract_rest_service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;
import com.gillsoft.model.service.ResourceInfoService;

public abstract class AbstractResourceService implements ResourceInfoService {
	
	@Override
	@GetMapping(Method.PING)
	public final Ping ping(@RequestParam("id") String id) {
		return pingResponse(id);
	}
	
	public abstract Ping pingResponse(String id);
	
	protected Ping createPing(String id) {
		Ping ping = new Ping();
		ping.setId(id);
		return ping;
	}
	
	@Override
	@GetMapping(Method.INFO)
	public final Resource getInfo() {
		return getInfoResponse();
	}
	
	public abstract Resource getInfoResponse();
	
	@Override
	@GetMapping(Method.METHOD)
	public final List<Method> getAvailableMethods() {
		return getAvailableMethodsResponse();
	}
	
	public abstract List<Method> getAvailableMethodsResponse();
	
	protected void addMethod(List<Method> methods, String name, String url, MethodType type) {
		Method method = new Method();
		method.setName(name);
		method.setUrl(url);
		method.setType(type);
		methods.add(method);
	}

}
