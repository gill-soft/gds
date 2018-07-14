package com.gillsoft.core.service.rest;

import java.net.URI;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.model.Document;
import com.gillsoft.model.Method;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Tariff;
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
		return getExchange(tripId, Method.SEARCH_TRIP_ROUTE, new ParameterizedTypeReference<Route>() { });
	}

	@Override
	public SeatsScheme getSeatsScheme(String tripId) {
		return getExchange(tripId, Method.SEARCH_TRIP_SEATS_SCHEME, new ParameterizedTypeReference<SeatsScheme>() { });
	}

	@Override
	public List<Seat> getSeats(String tripId) {
		return getExchange(tripId, Method.SEARCH_TRIP_SEATS, new ParameterizedTypeReference<List<Seat>>() { });
	}

	@Override
	public List<Tariff> getTariffs(String tripId) {
		return getExchange(tripId, Method.SEARCH_TRIP_TARIFFS, new ParameterizedTypeReference<List<Tariff>>() { });
	}

	@Override
	public List<RequiredField> getRequiredFields(String tripId) {
		return getExchange(tripId, Method.SEARCH_TRIP_REQUIRED, new ParameterizedTypeReference<List<RequiredField>>() { });
	}
	
	private <T> T getExchange(String tripId, String method, ParameterizedTypeReference<T> type) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(1);
		params.add("tripId", tripId);
		return getExchange(params, method, type);
	}
	
	private <T> T getExchange(MultiValueMap<String, String> params, String method, ParameterizedTypeReference<T> type) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + method)
				.queryParams(params).build().toUri();
		RequestEntity<?> entity = new RequestEntity<>(HttpMethod.GET, uri);
		ResponseEntity<T> response = resourceService.getTemplate()
				.exchange(entity, type);
		return response.getBody();
	}

	@Override
	public List<Seat> updateSeats(String tripId, List<Seat> seats) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReturnCondition> getConditions(String tripId, String tariffId) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(1);
		params.add("tripId", tripId);
		params.add("tariffId", tariffId);
		return getExchange(params, Method.SEARCH_TRIP_CONDITIONS, new ParameterizedTypeReference<List<ReturnCondition>>() { });
	}

	@Override
	public List<Document> getDocuments(String tripId) {
		return getExchange(tripId, Method.SEARCH_TRIP_DOCUMENTS, new ParameterizedTypeReference<List<Document>>() { });
	}
	
	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}

}
