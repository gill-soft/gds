package com.gillsoft.service;

import java.util.List;

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

public interface AgregatorTripSearchService {
	
	/**
	 * Метод инициализации поиска. Запускает поиск в ресурсе по указанным
	 * параметрам. Формирует уникальный ИД результата поиска для его получения.
	 * 
	 * @param request
	 *            Запрос инициализации поиска.
	 * @return Ответ с ИД поиска.
	 */
	public TripSearchResponse initSearch(List<TripSearchRequest> request);
	
	/**
	 * Нужно много написать //TODO
	 * @param searchId
	 * @return
	 */
	public TripSearchResponse getSearchResult(String searchId);
	
	public List<RouteResponse> getRoute(List<TripDetailsRequest> requests);
	
	public List<SeatsSchemeResponse> getSeatsScheme(List<TripDetailsRequest> requests);
	
	public List<SeatsResponse> getSeats(List<TripDetailsRequest> requests);
	
	public List<TariffsResponse> getTariffs(List<TripDetailsRequest> requests);
	
	public List<RequiredResponse> getRequiredFields(List<TripDetailsRequest> requests);
	
	public List<SeatsResponse> updateSeats(List<TripDetailsRequest> requests);
	
	public List<ReturnConditionResponse> getConditions(List<TripDetailsRequest> requests);
	
	public List<TripDocumentsResponse> getDocuments(List<TripDetailsRequest> requests);

}
