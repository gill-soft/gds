package com.gillsoft.core.service.rest;

import java.net.URI;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.model.Document;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.Method;
import com.gillsoft.model.Required;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.model.service.TripSearchService;

public class RestTripSearchService implements TripSearchService {
	
	private RestResourceService resourceService;

	@Override
	public TripSearchResponse initSearch(TripSearchRequest request) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.SEARCH)
				.build().toUri();
		ResponseEntity<TripSearchResponse> response = resourceService.getTemplate()
				.postForEntity(uri, request, TripSearchResponse.class);
		return response.getBody();
	}

	@Override
	public TripSearchResponse getSearchResult(String searchId) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.SEARCH)
				.queryParam("searchId", searchId)
				.build().toUri();
		ResponseEntity<TripSearchResponse> response = resourceService.getTemplate()
				.getForEntity(uri, TripSearchResponse.class);
		return response.getBody();
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
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.SEARCH_TRIP_SEATS)
				.queryParam("tripId", tripId)
				.build().toUri();
		RequestEntity<?> entity = new RequestEntity<>(HttpMethod.GET, uri);
		ParameterizedTypeReference<List<Seat>> type = new ParameterizedTypeReference<List<Seat>>() { };
		ResponseEntity<List<Seat>> response = resourceService.getTemplate()
				.exchange(entity, type);
		return response.getBody();
	}

	@Override
	public List<Tariff> getTariffs(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Required getRequiredFields(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Seat> updateSeats(String tripId, List<Seat> seats) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReturnCondition> getConditions(String tripId, String tariffId) {
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
