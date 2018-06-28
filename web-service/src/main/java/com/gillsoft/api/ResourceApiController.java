package com.gillsoft.api;

import java.rmi.AccessException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.core.ResourceController;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.ResourceMethodResponse;
import com.gillsoft.model.response.ResourceResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/resource")
@Api(tags = { "Resources" }, produces = "application/json", consumes = "application/json")
public class ResourceApiController {
	
	@Autowired
	private ResourceController controller;

	@ApiOperation(value = "Information about resource", response = Resource.class)
	@PostMapping
	public List<ResourceResponse> getResources(@Validated @RequestBody List<ResourceRequest> request) {
		return controller.getResources(request);
	}

	@ApiOperation(value = "Information about available API methods of resource",
			response = ResourceMethodResponse.class, responseContainer = "List")
	@PostMapping("/method")
	public List<ResourceMethodResponse> getMethods(
			@Validated @RequestBody List<ResourceRequest> request) throws AccessException {
		return controller.getMethods(request);
	}

}
