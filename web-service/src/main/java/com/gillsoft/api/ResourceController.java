package com.gillsoft.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.model.Method;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.store.ResourceStore;

@RestController
@RequestMapping("/resource")
@Api(tags = { "Resources" }, produces = "application/json")
public class ResourceController {

	@Autowired
	private ResourceStore store;

	@ApiOperation(value = "Information about resource", response = Resource.class)
	@PostMapping
	public Resource getResource(@Validated @RequestBody ResourceRequest request) {
		return store.getResourceService(request.getParams()).getInfo();
	}

	@ApiOperation(value = "Information about available API methods of resource",
			response = Method.class, responseContainer = "List")
	@PostMapping("/method")
	public List<Method> getMethods(
			@Validated @RequestBody ResourceRequest request) {
		return store.getResourceService(request.getParams())
				.getAvailableMethods();
	}

}
