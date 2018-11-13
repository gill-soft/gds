package com.gillsoft.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

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

	@Override
	public TripSearchResponse initSearch(List<TripSearchRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TripSearchResponse getSearchResult(String searchId) {
		// TODO Auto-generated method stub
		return null;
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

}
