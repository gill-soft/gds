package com.gillsoft.abstract_rest_service;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gillsoft.model.Method;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;
import com.gillsoft.model.service.ResourceInfoService;

public abstract class AbstractResourceService implements ResourceInfoService {
	
	@GetMapping(Method.PING)
	public abstract Ping ping(@Validated @RequestParam String id);
	
	@GetMapping(Method.INFO)
	public abstract Resource getInfo();
	
	@GetMapping(Method.METHOD)
	public abstract List<Method> getAvailableMethods();

}
