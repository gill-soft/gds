package com.gillsoft.core.service.rest;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

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

public class RestTripSearchService implements TripSearchService {
	
	private RestResourceService resourceService;

	@Override
	public TripSearchResponse initSearch(TripSearchRequest request) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.SEARCH_INIT)
				.build().toUri();
		ResponseEntity<TripSearchResponse> response = resourceService.getTemplate()
				.postForEntity(uri, request, TripSearchResponse.class);
		return response.getBody();
	}

	@Override
	public TripSearchResponse getSearchResult(String searchId) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.SEARCH_RESULT)
				.queryParam("searchId", searchId)
				.build().toUri();
		ResponseEntity<TripSearchResponse> response = resourceService.getTemplate()
				.getForEntity(uri, TripSearchResponse.class);
		return response.getBody();
	}

	@Override
	public Trip getInfo(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Route getRoute(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SeatsScheme getSeatsScheme(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Seat> getSeats(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Fare> getFares(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Required getRequiredFields(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Seat updateSeat(String tripId, List<Seat> seats) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReturnCondition> getConditions(String tripId, String fareId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Document> getDocuments(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}

}
