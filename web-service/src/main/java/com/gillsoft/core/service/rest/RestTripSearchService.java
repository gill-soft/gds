package com.gillsoft.core.service.rest;

import java.util.List;

import com.gillsoft.model.Document;
import com.gillsoft.model.Fare;
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
	public String initSearch(TripSearchRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TripSearchResponse getSearchResult(String searchId) {
		// TODO Auto-generated method stub
		return null;
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
	public Seat updateSeat(String tripId, Seat seat) {
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
