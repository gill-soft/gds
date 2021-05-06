package com.gillsoft.control.service.impl;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.AgregatorTripSearchService;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.request.TripDetailsRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.DocumentsResponse;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.RouteResponse;
import com.gillsoft.model.response.SeatsResponse;
import com.gillsoft.model.response.SeatsSchemeResponse;
import com.gillsoft.model.response.TariffsResponse;
import com.gillsoft.model.response.TripSearchResponse;

@Service
public class AgregatorTripSearchRestService extends AbstractAgregatorRestService implements AgregatorTripSearchService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorTripSearchRestService.class);
	
	private static final String SEARCH = "search";
	
	private static final String ROUTE = "search/trip/route";
	
	private static final String SEATS = "search/trip/seats";
	
	private static final String SCHEME = "search/trip/seats/scheme";
	
	private static final String TARIFFS = "search/trip/tariffs";
	
	private static final String REQUIRED = "search/trip/required";
	
	private static final String CONDITIONS = "search/trip/conditions";
	
	private static final String DOCUMENTS = "search/trip/documents";
	
	@Override
	public TripSearchResponse initSearch(List<TripSearchRequest> request) {
		try {
			return getResult(request, new ParameterizedTypeReference<TripSearchResponse>() { }, SEARCH);
		} catch (ResponseError e) {
			return new TripSearchResponse(null, e);
		}
	}

	@Override
	public TripSearchResponse getSearchResult(String searchId) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + SEARCH)
				.queryParam("searchId", searchId).build().toUri();
		RequestEntity<Object> entity = new RequestEntity<Object>(HttpMethod.GET, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<TripSearchResponse>() { });
		} catch (ResponseError e) {
			return new TripSearchResponse(null, e);
		}
	}

	@Override
	public List<RouteResponse> getRoute(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<RouteResponse>>() { }, ROUTE);
		} catch (ResponseError e) {
			return Collections.singletonList(new RouteResponse(null, e));
		}
	}

	@Override
	public List<SeatsSchemeResponse> getSeatsScheme(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<SeatsSchemeResponse>>() { }, SCHEME);
		} catch (ResponseError e) {
			return Collections.singletonList(new SeatsSchemeResponse(null, e));
		}
	}

	@Override
	public List<SeatsResponse> getSeats(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<SeatsResponse>>() { }, SEATS);
		} catch (ResponseError e) {
			return Collections.singletonList(new SeatsResponse(null, e));
		}
	}

	@Override
	public List<TariffsResponse> getTariffs(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<TariffsResponse>>() { }, TARIFFS);
		} catch (ResponseError e) {
			return Collections.singletonList(new TariffsResponse(null, e));
		}
	}

	@Override
	public List<RequiredResponse> getRequiredFields(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<RequiredResponse>>() { }, REQUIRED);
		} catch (ResponseError e) {
			return Collections.singletonList(new RequiredResponse(null, e));
		}
	}

	@Override
	public List<SeatsResponse> updateSeats(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<SeatsResponse>>() { }, SEATS, HttpMethod.PUT);
		} catch (ResponseError e) {
			return Collections.singletonList(new SeatsResponse(null, e));
		}
	}

	@Override
	public List<ReturnConditionResponse> getConditions(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<ReturnConditionResponse>>() { }, CONDITIONS);
		} catch (ResponseError e) {
			return Collections.singletonList(new ReturnConditionResponse(null, e));
		}
	}

	@Override
	public List<DocumentsResponse> getDocuments(List<TripDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<DocumentsResponse>>() { }, DOCUMENTS);
		} catch (ResponseError e) {
			return Collections.singletonList(new DocumentsResponse(null, e));
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
	
	private <T> T getResult(List<? extends ResourceRequest> request, ParameterizedTypeReference<T> type, String method) throws ResponseError {
		return getResult(request, type, method, HttpMethod.POST);
	}
	
	private <T> T getResult(List<? extends ResourceRequest> request, ParameterizedTypeReference<T> type, String method, HttpMethod httpMethod) throws ResponseError {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + method).build().toUri();
		RequestEntity<List<? extends ResourceRequest>> entity = new RequestEntity<List<? extends ResourceRequest>>(request, httpMethod, uri);
		return getResult(entity, type);
	}

}
