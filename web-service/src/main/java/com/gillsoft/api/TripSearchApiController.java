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
import com.gillsoft.model.request.SeatsRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.SeatsResponse;
import com.gillsoft.model.response.TripSearchResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/search")
@Api(tags = { "Trip search" }, produces = "application/json")
public class TripSearchApiController {
	
	@Autowired
	private TripSearchController controller;
	
	@ApiOperation(value = "Init search process",
			response = TripSearchResponse.class)
	@PostMapping
	public TripSearchResponse initSearch(@Validated @RequestBody List<TripSearchRequest> request) {
		return controller.initSearch(request);
	}
	
	@ApiOperation(value = "Return part of founded trips and link id to next result",
			response = TripSearchResponse.class)
	@GetMapping
	public TripSearchResponse getSearchResult(@Validated @RequestParam("searchId") String searchId) {
		return controller.getSearchResult(searchId);
	}
	
	@ApiOperation(value = "Return list of trip seats",
			response = SeatsResponse.class, responseContainer="List")
	@PostMapping("/trip/seats")
	public List<SeatsResponse> getSeats(@Validated @RequestBody List<SeatsRequest> request) {
		return controller.getSeats(request);
	}

}
