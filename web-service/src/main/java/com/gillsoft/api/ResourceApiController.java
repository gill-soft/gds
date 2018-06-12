package com.gillsoft.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.rmi.AccessException;
import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.core.ResourceController;
import com.gillsoft.model.Method;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceRequest;

@RestController
@RequestMapping("/resource")
@Api(tags = { "Resources" }, produces = "application/json")
public class ResourceApiController {

	@ApiOperation(value = "Information about resource", response = Resource.class)
	@PostMapping
	public List<Resource> getResources(@Validated @RequestBody List<ResourceRequest> request) {
		return ResourceController.getInstance().getResources(request);
	}

	@ApiOperation(value = "Information about available API methods of resource",
			response = Method.class, responseContainer = "List")
	@PostMapping("/method")
	public List<Method> getMethods(
			@Validated @RequestBody ResourceRequest request) throws AccessException {
		return ResourceController.getInstance().getMethods(request);
	}

}
