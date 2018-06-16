package com.gillsoft.model.service;

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

public interface TripSearchService {
	
	/**
	 * Метод инициализации поиска. Запускает поиск в ресурсе по указанным
	 * параметрам. Формирует уникальный ИД результата поиска для его получения.
	 * 
	 * @param request
	 *            Запрос инициализации поиска.
	 * @return Ответ с ИД поиска.
	 */
	public TripSearchResponse initSearch(TripSearchRequest request);
	
	/**
	 * Нужно много написать //TODO
	 * @param searchId
	 * @return
	 */
	public TripSearchResponse getSearchResult(String searchId);
	
	public Trip getInfo(String tripId);
	
	public Route getRoute(String tripId);
	
	public SeatsScheme getSeatsScheme(String tripId);
	
	public List<Seat> getSeats(String tripId);
	
	public List<Fare> getFares(String tripId);
	
	public Required getRequiredFields(String tripId);
	
	public Seat updateSeat(String tripId, List<Seat> seats);
	
	public List<ReturnCondition> getConditions(String tripId, String fareId);
	
	public List<Document> getDocuments(String tripId);

}
