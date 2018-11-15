package com.gillsoft.service.impl;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.config.Config;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.request.TripDetailsRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.RouteResponse;
import com.gillsoft.model.response.SeatsResponse;
import com.gillsoft.model.response.SeatsSchemeResponse;
import com.gillsoft.model.response.TariffsResponse;
import com.gillsoft.model.response.TripDocumentsResponse;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.service.AgregatorTripSearchService;

@Service
public class AgregatorTripSearchRestService extends AbstractAgregatorRestService implements AgregatorTripSearchService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorTripSearchRestService.class);
	
	private static final String SEARCH_INIT = "search";
	
	private static final String GET_SEARCH = "search/{searchId}";

	@Override
	public TripSearchResponse initSearch(List<TripSearchRequest> request) {
		try {
			return getResult(request, new ParameterizedTypeReference<TripSearchResponse>() { }, SEARCH_INIT);
		} catch (ResponseError e) {
			return new TripSearchResponse(null, e);
		}
	}

	@Override
	public TripSearchResponse getSearchResult(String searchId) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + GET_SEARCH)
				.queryParam("searchId", searchId).build().toUri();
		RequestEntity<Object> entity = new RequestEntity<Object>(HttpMethod.GET, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<TripSearchResponse>() { });
		} catch (ResponseError e) {
			return null;
		}
	}

	@Override
	public List<RouteResponse> getRoute(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SeatsSchemeResponse> getSeatsScheme(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SeatsResponse> getSeats(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TariffsResponse> getTariffs(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<RequiredResponse> getRequiredFields(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SeatsResponse> updateSeats(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ReturnConditionResponse> getConditions(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TripDocumentsResponse> getDocuments(List<TripDetailsRequest> requests) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
	
	private <T> T getResult(List<? extends ResourceRequest> request, ParameterizedTypeReference<T> type, String method) throws ResponseError {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + method).build().toUri();
		RequestEntity<List<? extends ResourceRequest>> entity = new RequestEntity<List<? extends ResourceRequest>>(request, HttpMethod.POST, uri);
		return getResult(entity, type);
	}

}
