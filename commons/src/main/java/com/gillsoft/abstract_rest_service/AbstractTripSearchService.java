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
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.model.service.TripSearchService;

public abstract class AbstractTripSearchService implements TripSearchService {

	@Override
	@PostMapping(Method.SEARCH)
	public final TripSearchResponse initSearch(@RequestBody TripSearchRequest request) {
		return initSearchResponse(request);
	}
	
	public abstract TripSearchResponse initSearchResponse(TripSearchRequest request);

	@Override
	@GetMapping(Method.SEARCH)
	public final TripSearchResponse getSearchResult(@RequestParam("searchId") String searchId) {
		return getSearchResultResponse(searchId);
	}
	
	public abstract TripSearchResponse getSearchResultResponse(String searchId);

	@Override
	@GetMapping(Method.SEARCH_TRIP_ROUTE)
	public final Route getRoute(@RequestParam("tripId") String tripId) {
		return getRouteResponse(tripId);
	}
	
	public abstract Route getRouteResponse(String tripId);

	@Override
	@GetMapping(Method.SEARCH_TRIP_SEATS_SCHEME)
	public final SeatsScheme getSeatsScheme(@RequestParam("tripId") String tripId) {
		return getSeatsSchemeResponse(tripId);
	}
	
	public abstract SeatsScheme getSeatsSchemeResponse(String tripId);

	@Override
	@GetMapping(Method.SEARCH_TRIP_SEATS)
	public final List<Seat> getSeats(@RequestParam("tripId") String tripId) {
		return getSeatsResponse(tripId);
	}
	
	public abstract List<Seat> getSeatsResponse(String tripId);

	@Override
	@GetMapping(Method.SEARCH_TRIP_FARES)
	public final List<Fare> getFares(@RequestParam("tripId") String tripId) {
		return getFaresResponse(tripId);
	}
	
	public abstract List<Fare> getFaresResponse(String tripId);

	@Override
	@GetMapping(Method.SEARCH_TRIP_REQUIRED)
	public final Required getRequiredFields(@RequestParam("tripId") String tripId) {
		return getRequiredFieldsResponse(tripId);
	}
	
	public abstract Required getRequiredFieldsResponse(String tripId);

	@Override
	@PostMapping(Method.SEARCH_TRIP_SEATS)
	public final List<Seat> updateSeats(@RequestParam("tripId") String tripId, @RequestBody List<Seat> seats) {
		return updateSeatsResponse(tripId, seats);
	}
	
	public abstract List<Seat> updateSeatsResponse(String tripId, List<Seat> seats);

	@Override
	@GetMapping(Method.SEARCH_TRIP_CONDITIONS)
	public final List<ReturnCondition> getConditions(@RequestParam("tripId") String tripId,
			@RequestParam("fareId") String fareId) {
		return getConditionsResponse(tripId, fareId);
	}
	
	public abstract List<ReturnCondition> getConditionsResponse(String tripId, String fareId);

	@Override
	@GetMapping(Method.SEARCH_TRIP_DOCUMENTS)
	public final List<Document> getDocuments(@RequestParam("tripId") String tripId) {
		return getDocumentsResponse(tripId);
	}
	
	public abstract List<Document> getDocumentsResponse(String tripId);

}
