package com.gillsoft.abstract_service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gillsoft.model.Method;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;

public abstract class AbstractResourceService {
	
	@GetMapping(Method.PING)
	public abstract Ping ping(@RequestParam("id") String id);
	
	@GetMapping(Method.INFO)
	public abstract Resource getInfo();
	
	@GetMapping(Method.METHOD)
	public abstract List<Method> getMethods();

}
