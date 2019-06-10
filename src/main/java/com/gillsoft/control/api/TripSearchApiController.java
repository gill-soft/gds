package com.gillsoft.control.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.control.core.TripSearchController;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.request.TripSearchRequest;
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
	
	@ApiOperation(value = "Init search process", response = TripSearchResponse.class)
	@PostMapping
	public TripSearchResponse initSearch(@Validated @RequestBody TripSearchRequest request) {
		return controller.initSearch(request);
	}
	
	@ApiOperation(value = "Returns part of founded trips and link id to next result",
			response = TripSearchResponse.class)
	@GetMapping("/{searchId}")
	public TripSearchResponse getSearchResult(@ApiParam(value = "The search id from init search request", required = true)
			@Validated @PathVariable String searchId) {
		return controller.getSearchResult(searchId);
	}
	
	@ApiOperation(value = "Returns the list of trip seats",
			response = Seat.class, responseContainer="List")
	@GetMapping("/trip/{tripId}/seats")
	public List<Seat> getSeats(@Validated @PathVariable String tripId) {
		return controller.getSeats(tripId);
	}
	
	@ApiOperation(value = "Returns the list of trip seats", response = SeatsScheme.class)
	@GetMapping("/trip/{tripId}/seats/scheme")
	public SeatsScheme getScheme(@Validated @PathVariable String tripId) {
		return controller.getSeatsScheme(tripId);
	}
	
	@ApiOperation(value = "Returns the list of trip tariffs",
			response = Tariff.class, responseContainer="List")
	@GetMapping("/trip/{tripId}/tariffs/{lang}")
	public List<Tariff> getTariffs(@Validated @PathVariable String tripId,
			@PathVariable(required = false) Lang lang) {
		return controller.getTariffs(tripId, lang);
	}
	
	@ApiOperation(value = "Returns the route of trip", response = Route.class)
	@GetMapping("/trip/{tripId}/route/{lang}")
	public Route getRoute(@Validated @PathVariable String tripId,
			@PathVariable(required = false) Lang lang) {
		return controller.getRoute(tripId, lang);
	}
	
	@ApiOperation(value = "Returns the list of required fields to create order to this trip",
			response = RequiredField.class, responseContainer="List")
	@GetMapping("/trip/{tripId}/required")
	public List<RequiredField> getRequiredFields(@Validated @PathVariable String tripId) {
		return controller.getRequiredFields(tripId);
	}
	
	@ApiOperation(value = "Returns the list of updated seats to this trip",
			response = Seat.class, responseContainer="List")
	@PostMapping("/trip/{tripId}/seats")
	public List<Seat> updateSeats(@Validated @PathVariable String tripId, @Validated @RequestBody List<Seat> seats) {
		return controller.updateSeats(tripId, seats);
	}
	
	@ApiOperation(value = "Returns the list of tariff return conditions to this trip and selected tariff",
			response = ReturnCondition.class, responseContainer="List")
	@GetMapping("/trip/{tripId}/conditions/{tariffId}/{lang}")
	public List<ReturnCondition> getReturnConditions(@Validated @PathVariable String tripId,
			@Validated @PathVariable("tariffId") String tariffId,
			@PathVariable(required = false) Lang lang) {
		return controller.getConditions(tripId, tariffId, lang);
	}
	
	@ApiOperation(value = "Returns the list of some documents about trip",
			response = Document.class, responseContainer="List")
	@GetMapping("/trip/{tripId}/documents/{lang}")
	public List<Document> getDocuments(@Validated @PathVariable String tripId,
			@PathVariable(required = false) Lang lang) {
		return controller.getDocuments(tripId, lang);
	}

}
