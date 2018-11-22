package com.gillsoft.control.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.control.core.LocalityController;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.request.LocalityRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/locality")
@Api(tags = { "Localities" }, produces = "application/json", consumes = "application/json")
public class LocalityApiController {
	
	@Autowired
	private LocalityController controller;

	@ApiOperation(value = "The list of all cities, stations, stoppings, etc wich are available for user",
			response = Locality.class, responseContainer = "List")
	@GetMapping("/all/{lang}")
	public List<Locality> getAll(@ApiParam(value = "The lang of requested data", required = true)
			@PathVariable(required = false) Lang lang) {
		return controller.getAll(createRequest(lang));
	}
	
	@ApiOperation(value = "The list of all cities, stations, stoppings, etc wich are available for user",
			response = Locality.class, responseContainer = "List")
	@GetMapping("/all")
	public List<Locality> getAll() {
		return controller.getAll(createRequest(null));
	}

	@ApiOperation(value = "The list of used cities, stations, stoppings, etc",
			response = Locality.class, responseContainer = "List")
	@GetMapping("/used/{lang}")
	public List<Locality> getUsed(@ApiParam(value = "The lang of requested data", required = true)
			@PathVariable(required = false) Lang lang) {
		return controller.getUsed(createRequest(lang));
	}
	
	@ApiOperation(value = "The list of used cities, stations, stoppings, etc",
			response = Locality.class, responseContainer = "List")
	@GetMapping("/used")
	public List<Locality> getUsed() {
		return controller.getUsed(createRequest(null));
	}

	@ApiOperation(value = "The binding beetwen cities, stations, stoppings, etc", responseContainer = "Map")
	@GetMapping("/binding")
	public Map<String, Set<String>> getBinding() {
		return controller.getBinding(null);
	}
	
	private LocalityRequest createRequest(Lang lang) {
		LocalityRequest request = new LocalityRequest();
		request.setLang(lang);
		return request;
	}

}
