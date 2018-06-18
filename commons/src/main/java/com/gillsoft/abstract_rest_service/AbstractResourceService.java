package com.gillsoft.abstract_rest_service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gillsoft.model.Method;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;
import com.gillsoft.model.service.ResourceInfoService;

public abstract class AbstractResourceService implements ResourceInfoService {
	
	@GetMapping(Method.PING)
	public final Ping ping(@RequestParam("id") String id) {
		return ping(id);
	}
	
	public abstract Ping pingResponse(String id);
	
	@GetMapping(Method.INFO)
	public final Resource getInfo() {
		return getInfoResponse();
	}
	
	public abstract Resource getInfoResponse();
	
	@GetMapping(Method.METHOD)
	public final List<Method> getAvailableMethods() {
		return getAvailableMethodsResponse();
	}
	
	public abstract List<Method> getAvailableMethodsResponse();

}
