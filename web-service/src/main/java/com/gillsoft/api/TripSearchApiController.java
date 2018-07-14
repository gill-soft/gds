package com.gillsoft.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.core.TripSearchController;
import com.gillsoft.model.request.TripDetailsRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.RouteResponse;
import com.gillsoft.model.response.SeatsResponse;
import com.gillsoft.model.response.TariffsResponse;
import com.gillsoft.model.response.TripSearchResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/search")
@Api(tags = { "Trip search" }, produces = "application/json", consumes = "application/json")
public class TripSearchApiController {
	
	@Autowired
	private TripSearchController controller;
	
	@ApiOperation(value = "Init search process",
			response = TripSearchResponse.class)
	@PostMapping
	public TripSearchResponse initSearch(@Validated @RequestBody List<TripSearchRequest> request) {
		return controller.initSearch(request);
	}
	
	@ApiOperation(value = "Returns part of founded trips and link id to next result",
			response = TripSearchResponse.class)
	@GetMapping
	public TripSearchResponse getSearchResult(@ApiParam(value = "The search id from init search request", required = true)
			@Validated @RequestParam("searchId") String searchId) {
		return controller.getSearchResult(searchId);
	}
	
	@ApiOperation(value = "Returns the list of trip seats",
			response = SeatsResponse.class, responseContainer="List")
	@PostMapping("/trip/seats")
	public List<SeatsResponse> getSeats(@Validated @RequestBody List<TripDetailsRequest> request) {
		return controller.getSeats(request);
	}
	
	@ApiOperation(value = "Returns the list of trip tariffs",
			response = SeatsResponse.class, responseContainer="List")
	@PostMapping("/trip/tariffs")
	public List<TariffsResponse> getTariffs(@Validated @RequestBody List<TripDetailsRequest> request) {
		return controller.getTariffs(request);
	}
	
	@ApiOperation(value = "Returns the route of trip",
			response = SeatsResponse.class, responseContainer="List")
	@PostMapping("/trip/route")
	public List<RouteResponse> getRoute(@Validated @RequestBody List<TripDetailsRequest> request) {
		return controller.getRoutes(request);
	}
	
	@ApiOperation(value = "Returns the list of required fields to create order to this trip",
			response = SeatsResponse.class, responseContainer="List")
	@PostMapping("/trip/required")
	public List<RequiredResponse> getRequired(@Validated @RequestBody List<TripDetailsRequest> request) {
		return controller.getRequired(request);
	}

}
