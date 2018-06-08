package com.gillsoft.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.model.Location;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.store.ResourceStore;

@RestController
@RequestMapping("/location")
@Api(tags = { "Locations" }, produces = "application/json")
public class LocationController {

	@Autowired
	private ResourceStore store;

	@ApiOperation(value = "The list of all cities, stations, stoppings, etc of resource",
			response = Location.class, responseContainer = "List")
	@PostMapping("/all")
	public List<Location> getAll(@Validated @RequestBody ResourceRequest request) {
		return store.getResourceService(request.getParams())
				.getLocationService().getAll();
	}

	@ApiOperation(value = "The list of used cities, stations, stoppings, etc of resource wich can use in search",
			response = Location.class, responseContainer = "List")
	@PostMapping("/used")
	public List<Location> getUsed(
			@Validated @RequestBody ResourceRequest request) {
		return store.getResourceService(request.getParams())
				.getLocationService().getUsed();
	}

	@ApiOperation("The binding beetwen cities, stations, stoppings, etc of resource")
	@PostMapping("/binding")
	public Map<String, List<String>> getBinding(
			@Validated @RequestBody ResourceRequest request) {
		return store.getResourceService(request.getParams())
				.getLocationService().getBinding();
	}

}
