package com.gillsoft.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.core.LocalityController;
import com.gillsoft.mapper.model.Mapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/locality")
@Api(tags = { "Localities" }, produces = "application/json", consumes = "application/json")
public class LocalityApiController {
	
	@Autowired
	private LocalityController controller;

	@ApiOperation(value = "The list of all cities, stations, stoppings, etc wich are available for user",
			response = Mapping.class, responseContainer = "List")
	@PostMapping("/all")
	public List<Mapping> getAll() {
		return controller.getAll();
	}

	@ApiOperation(value = "The list of used cities, stations, stoppings, etc",
			response = Mapping.class)
	@PostMapping("/used")
	public List<Mapping> getUsed() {
		return controller.getUsed();
	}

	@ApiOperation(value = "The binding beetwen cities, stations, stoppings, etc", responseContainer = "Map")
	@PostMapping("/binding")
	public Map<Long, Set<Long>> getBinding() {
		return controller.getBinding();
	}

}
