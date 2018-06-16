package com.gillsoft.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.core.LocalityController;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/locality")
@Api(tags = { "Localities" }, produces = "application/json")
public class LocalityApiController {
	
	@Autowired
	private LocalityController controller;

	@ApiOperation(value = "The list of all cities, stations, stoppings, etc of resource",
			response = LocalityResponse.class, responseContainer = "List")
	@PostMapping("/all")
	public List<LocalityResponse> getAll(@Validated @RequestBody List<LocalityRequest> request) {
		return controller.getAll(request);
	}

	@ApiOperation(value = "The list of used cities, stations, stoppings, etc of resource wich can use in search",
			response = LocalityResponse.class, responseContainer = "List")
	@PostMapping("/used")
	public List<LocalityResponse> getUsed(
			@Validated @RequestBody List<LocalityRequest> request) {
		return controller.getUsed(request);
	}

	@ApiOperation(value = "The binding beetwen cities, stations, stoppings, etc of resource",
			response = LocalityResponse.class, responseContainer = "List")
	@PostMapping("/binding")
	public List<LocalityResponse> getBinding(
			@Validated @RequestBody List<LocalityRequest> request) {
		return controller.getBinding(request);
	}

}
