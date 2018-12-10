package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.AgregatorTripSearchService;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.RoutePoint;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.Request;
import com.gillsoft.model.request.TripDetailsRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.Response;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.RouteResponse;
import com.gillsoft.model.response.SeatsResponse;
import com.gillsoft.model.response.SeatsSchemeResponse;
import com.gillsoft.model.response.TariffsResponse;
import com.gillsoft.model.response.TripDocumentsResponse;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;
import com.google.common.base.Objects;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TripSearchController {
	
	private static Logger LOGGER = LogManager.getLogger(TripSearchController.class);
	
	@Autowired
    @Qualifier("MemoryCacheHandler")
	private CacheHandler cache;
	
	@Autowired
	private AgregatorTripSearchService service;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private MappingService mappingService;
	
	@Autowired
	private TripSearchMapping tripSearchMapping;
	
	/**
	 * Запускает поиск по запросу с АПИ. Конвертирует обобщенный запрос в запросы ко всем доступным ресурсам.
	 */
	public TripSearchResponse initSearch(TripSearchRequest request) {
		
		// проверяем параметры запроса
		validateSearchRequest(request);
		
		return initSearch(createSearchRequest(request));
	}
	
	/**
	 * Запускает поиск по запросам к конкретным ресурсам.
	 */
	public TripSearchResponse initSearch(List<TripSearchRequest> requests) {
		
		// проверяем ответ и записываем в память запросы под ид поиска
		TripSearchResponse response = checkResponse(null, service.initSearch(requests));
		putRequestToCache(response.getSearchId(), requests);
		return response;
	}
	
	private void validateSearchRequest(TripSearchRequest request) {
		if (request.getLocalityPairs() == null
				|| request.getLocalityPairs().isEmpty()) {
			throw new RequestValidateException("Empty localityPairs");
		}
		for (String[] pair : request.getLocalityPairs()) {
			if (pair.length != 2) {
				throw new RequestValidateException("localityPairs has invalid pair " + Arrays.toString(pair));
			}
		}
		if (request.getDates() == null
				|| request.getDates().isEmpty()) {
			throw new RequestValidateException("Empty dates");
		}
	}
	
	private void putRequestToCache(String searchId, List<TripSearchRequest> requests) {
		if (searchId != null) {
			Map<String, Object> params = new HashMap<>();
			params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
			params.put(MemoryCacheHandler.TIME_TO_LIVE, 60000l);
			try {
				cache.write(requests, params);
			} catch (IOCacheException e) {
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public TripSearchResponse getSearchResult(String searchId) {
		
		// получает запросы поиска с памяти по ид поиска и проверяем их
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		List<TripSearchRequest> requests = null;
		try {
			requests = (List<TripSearchRequest>) cache.read(params);
		} catch (ClassCastException | IOCacheException e) {
			LOGGER.error("Empty request by searchId: " + searchId, e);
		}
		if (requests == null) {
			LOGGER.error("Too late for getting result by searchId: " + searchId);
			throw new ApiException(new ResponseError("Too late for getting result or invalid searchId."));
		}
		TripSearchResponse response = service.getSearchResult(searchId);
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		}
		// добавляем в кэш запрос под новым searchId, для получения остального результата
		putRequestToCache(response.getSearchId(), requests);
		if (response.getResult() != null
				&& !response.getResult().isEmpty()) {
			
			TripSearchResponse result = new TripSearchResponse();
			result.setId(requests.get(0).getId().split(";")[0]);
			result.setSearchId(response.getSearchId());
			
			// создаем словари ответа
			tripSearchMapping.createDictionaries(result);
			
			for (TripSearchResponse searchResponse : response.getResult()) {
				
				Stream<TripSearchRequest> stream = requests.stream().filter(r -> r.getId().equals(searchResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					TripSearchRequest request = stream.findFirst().get();
					
					// проверяем ошибки
					if (searchResponse.getTripContainers() != null) {
						
						// логируем ошибки, если есть
						logError(searchResponse);
						
						// мапим словари
						tripSearchMapping.mapDictionaries(request, searchResponse, result);
						
						// обновляем мапингом рейсы
						tripSearchMapping.updateSegments(request, searchResponse, result);
					}
				}
			}
			// меняем ключи мап на ид из мапинга
			tripSearchMapping.updateResultDictionaries(result);
			return result;
		}
		return response;
	}
	
	private void logError(TripSearchResponse searchResponse) {
		for (TripContainer container : searchResponse.getTripContainers()) {
			if (container.getError() != null) {
				try {
					LOGGER.error("ERROR in response id: " + searchResponse.getId()
					+ "\n" + StringUtil.objectToJsonString(container.getError()));
				} catch (JsonProcessingException e) {
					LOGGER.error("ERROR in response id: " + container.getError());
				}
			}
		}
	}
	
	private List<TripSearchRequest> createSearchRequest(TripSearchRequest searchRequest) {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			List<TripSearchRequest> request = new ArrayList<>();
			for (Resource resource : resources) {
				if (infoController.isMethodAvailable(resource, Method.SEARCH, MethodType.POST)) {
					
					// проверяем маппинг и формируем запрос на каждый ресурс
					for (String[] pairs : searchRequest.getLocalityPairs()) {
						Set<String> fromIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pairs[0]));
						if (fromIds != null) {
							Set<String> toIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pairs[1]));
							if (toIds != null) {
								TripSearchRequest resourceSearchRequest = new TripSearchRequest();
								resourceSearchRequest.setId(searchRequest.getId() + ";" + StringUtil.generateUUID());
								resourceSearchRequest.setParams(resource.createParams());
								resourceSearchRequest.setDates(searchRequest.getDates());
								resourceSearchRequest.setBackDates(searchRequest.getBackDates());
								resourceSearchRequest.setCurrency(searchRequest.getCurrency());
								resourceSearchRequest.setLocalityPairs(new ArrayList<>());
								resourceSearchRequest.setLang(searchRequest.getLang());
								request.add(resourceSearchRequest);
								for (String fromId : fromIds) {
									for (String toId : toIds) {
										resourceSearchRequest.getLocalityPairs().add(new String[] { fromId, toId });
									}
								}
							}
						}
					}
				}
			}
			if (request.isEmpty()) {
				throw new MethodUnavalaibleException("Method is unavailable");
			}
			return request;
		}
		throw new ResourceUnavailableException("User does not has available resources");
	}
	
	public Route getRoute(String tripId, Lang lang) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, lang, Method.SEARCH_TRIP_ROUTE, MethodType.GET);
		RouteResponse response = checkResponse(requests.get(0), service.getRoute(requests).get(0));
		
		// мапим пункты маршрута
		Map<String, Locality> localities = new HashMap<>();
		Route route = response.getRoute();
		if (route.getPath() != null) {
			for (RoutePoint point : route.getPath()) {
				localities.put(point.getLocality().getId(), point.getLocality());
			}
			Map<String, Locality> mapped = new HashMap<>();
			tripSearchMapping.mappingGeo(requests.get(0), localities, mapped);
			for (RoutePoint point : route.getPath()) {
				point.setLocality(mapped.get(tripSearchMapping.getKey(
						requests.get(0).getParams().getResource().getId(), point.getLocality().getId())));
			}
		}
		return route;
	}
	
	public SeatsScheme getSeatsScheme(String tripId) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, null, Method.SEARCH_TRIP_SEATS_SCHEME, MethodType.GET);
		SeatsSchemeResponse response = checkResponse(requests.get(0), service.getSeatsScheme(requests).get(0));
		return response.getScheme();
	}
	
	public List<Seat> getSeats(String tripId) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, null, Method.SEARCH_TRIP_SEATS, MethodType.GET);
		SeatsResponse response = checkResponse(requests.get(0), service.getSeats(requests).get(0));
		return response.getSeats();
	}
	
	public List<Tariff> getTariffs(String tripId, Lang lang) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, lang, Method.SEARCH_TRIP_TARIFFS, MethodType.GET);
		TariffsResponse response = checkResponse(requests.get(0), service.getTariffs(requests).get(0));
		return response.getTariffs();
	}

	public List<RequiredField> getRequiredFields(String tripId) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, null, Method.SEARCH_TRIP_REQUIRED, MethodType.GET);
		RequiredResponse response = checkResponse(requests.get(0), service.getRequiredFields(requests).get(0));
		return response.getFields();
	}
	
	public List<Seat> updateSeats(String tripId, @RequestBody List<Seat> seats) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, null, Method.SEARCH_TRIP_SEATS, MethodType.POST);
		requests.get(0).setSeats(seats);
		SeatsResponse response = checkResponse(requests.get(0), service.updateSeats(requests).get(0));
		return response.getSeats();
	}
	
	public List<ReturnCondition> getConditions(String tripId, String tariffId, Lang lang) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, lang, Method.SEARCH_TRIP_CONDITIONS, MethodType.GET);
		requests.get(0).setTariffId(tariffId);
		ReturnConditionResponse response = checkResponse(requests.get(0), service.getConditions(requests).get(0));
		return response.getConditions();
	}
	
	public List<Document> getDocuments(String tripId, Lang lang) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, lang, Method.SEARCH_TRIP_DOCUMENTS, MethodType.GET);
		TripDocumentsResponse response = checkResponse(requests.get(0), service.getDocuments(requests).get(0));
		return response.getDocuments();
	}
	
	public List<RequiredResponse> getRequiredFields(List<TripDetailsRequest> requests) {
		return service.getRequiredFields(requests);
	}
	
	private <T extends Response> T checkResponse(Request request, T response) {
		if (request != null
				&& !Objects.equal(request.getId(), response.getId())) {
			throw new ApiException("The response does not match the request");
		}
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		} else {
			return response;
		}
	}
	
	public List<TripDetailsRequest> createTripDetailsRequests(String tripId, Lang lang, String methodPath, MethodType methodType) {
		return Collections.singletonList(createTripDetailsRequest(tripId, lang, methodPath, methodType));
	}
	
	public TripDetailsRequest createTripDetailsRequest(String tripId, Lang lang, String methodPath, MethodType methodType) {
		TripIdModel idModel = new TripIdModel().create(tripId);
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			for (Resource resource : resources) {
				if (resource.getId() == idModel.getResourceId()) {
					if (infoController.isMethodAvailable(resource, methodPath, methodType)) {
						TripDetailsRequest request = new TripDetailsRequest();
						request.setId(StringUtil.generateUUID());
						request.setLang(lang);
						request.setParams(resource.createParams());
						request.setTripId(idModel.getId());
						return request;
					} else {
						throw new MethodUnavalaibleException("Method for this trip is unavailable");
					}
				}
			}
		}
		throw new ResourceUnavailableException("Resource of this trip is unavailable for user");
	}
	
	public TripSearchResponse search(OrderRequest request, Set<String> tripIds) { 
		try {
			// создаем поиски
			List<TripSearchRequest> searchRequests = new ArrayList<>();
			for (String tripId : tripIds) {
				TripIdModel model = new TripIdModel().create(tripId);
				model.getRequest().setId(StringUtil.generateUUID());
				model.getRequest().setParams(dataController.createResourceParams(model.getResourceId()));
				model.getRequest().setLang(request.getLang());
				model.getRequest().setCurrency(request.getCurrency());
				searchRequests.add(model.getRequest());
			}
			TripSearchResponse response = initSearch(searchRequests);
			
			// получаем результат сразу же так как он уже в кэше
			response = getSearchResult(response.getSearchId());
			
			final Collection<Segment> segments = response.getSegments().values();
				
			// оставляем в запросе только указанный рейс
			if (response.getSegments() != null) {
				response.setTripContainers(null);
				response.getSegments().keySet().removeIf(key -> !tripIds.contains(key));
				response.getSegments().values().forEach(segment -> {
					segment.setRoute(null);
					segment.setSeats(null);
				});
			}
				
			// перезаливаем словари
			if (response.getVehicles() != null) {
				response.getVehicles().keySet().removeIf(key -> {
					for (Segment segment : segments) {
						if (segment.getVehicle() != null
								&& Objects.equal(key, segment.getVehicle().getId())) {
							return false;
						}
					}; return true;});
			}
			if (response.getOrganisations() != null) {
				response.getOrganisations().keySet().removeIf(key -> {
					for (Segment segment : segments) {
						if ((segment.getCarrier() != null
								&& Objects.equal(key, segment.getCarrier().getId()))
								|| (segment.getInsurance() != null
										&& Objects.equal(key, segment.getInsurance().getId()))) {
							return false;
						}
					}; return true;});
			}
			if (response.getLocalities() != null) {
				response.getLocalities().keySet().removeIf(key -> {
					for (Segment segment : segments) {
						if (Objects.equal(key, segment.getDeparture().getId())
								|| Objects.equal(key, segment.getArrival().getId())) {
							return false;
						}
					}; return true;});
			}
			return response;
		} catch (Exception e) {
			LOGGER.error("Error when search selected trips", e);
		}
		return null;
	}

}
