package com.gillsoft.abstract_rest_service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.gillsoft.model.Document;
import com.gillsoft.model.Fare;
import com.gillsoft.model.Method;
import com.gillsoft.model.Required;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Trip;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.model.service.TripSearchService;

public abstract class AbstractTripSearchService implements TripSearchService {

	@PostMapping(Method.SEARCH_INIT)
	public abstract TripSearchResponse initSearch(TripSearchRequest request);

	@GetMapping(Method.SEARCH_RESULT)
	public abstract TripSearchResponse getSearchResult(@RequestParam("searchId") String searchId);

	@GetMapping(Method.SEARCH_TRIP)
	public abstract Trip getInfo(@RequestParam("tripId") String tripId);

	@GetMapping(Method.SEARCH_TRIP_ROUTE)
	public abstract Route getRoute(@RequestParam("tripId") String tripId);

	@GetMapping(Method.SEARCH_TRIP_SEATS_SCHEME)
	public abstract SeatsScheme getSeatsScheme(@RequestParam("tripId") String tripId);

	@GetMapping(Method.SEARCH_TRIP_SEATS)
	public abstract List<Seat> getSeats(@RequestParam("tripId") String tripId);

	@GetMapping(Method.SEARCH_TRIP_FARES)
	public abstract List<Fare> getFares(@RequestParam("tripId") String tripId);

	@GetMapping(Method.SEARCH_TRIP_REQUIRED)
	public abstract Required getRequiredFields(@RequestParam("tripId") String tripId);

	@PostMapping(Method.SEARCH_TRIP_SEATS_UPDATE)
	public abstract Seat updateSeat(@RequestParam("tripId") String tripId, @RequestBody List<Seat> seats);

	@GetMapping(Method.SEARCH_TRIP_CONDITIONS)
	public abstract List<ReturnCondition> getConditions(@RequestParam("tripId") String tripId,
			@RequestParam("fareId") String fareId);

	@GetMapping(Method.SEARCH_TRIP_DOCUMENTS)
	public abstract List<Document> getDocuments(@RequestParam("tripId") String tripId);

}
