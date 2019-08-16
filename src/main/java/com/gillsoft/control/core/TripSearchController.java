package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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
import com.gillsoft.control.service.ScheduleService;
import com.gillsoft.control.service.model.SearchRequestContainer;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.RoutePoint;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.Request;
import com.gillsoft.model.request.TripDetailsRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.OrderResponse;
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

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TripSearchController {
	
	private static Logger LOGGER = LogManager.getLogger(TripSearchController.class);
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	@Autowired
	private AgregatorTripSearchService service;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private SearchRequestController requestController;
	
	@Autowired
	private TripSearchMapping tripSearchMapping;
	
	@Autowired
	private FilterController filter;
	
	@Autowired
	private ConnectionsController connectionsController;
	
	@Autowired
	private ScheduleService scheduleService;
	
	/**
	 * Запускает поиск по запросу с АПИ. Конвертирует обобщенный запрос в запросы ко всем доступным ресурсам.
	 */
	public TripSearchResponse initSearch(TripSearchRequest request) {
		
		// проверяем параметры запроса
		validateSearchRequest(request);
		
		return initSearch(requestController.createSearchRequest(request));
	}
	
	/**
	 * Запускает поиск по запросам к конкретным ресурсам.
	 */
	public TripSearchResponse initSearch(SearchRequestContainer requestContainer) {
		
		// проверяем ответ и записываем в память запросы под ид поиска
		TripSearchResponse response = checkResponse(null, service.initSearch(requestContainer.getRequests()));
		putRequestToCache(response.getSearchId(), requestContainer);
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
		if (request.isUseTranfers()
				&& (request.getLocalityPairs().size() > 1
						|| request.getDates().size() > 1)) {
			throw new RequestValidateException("Search with transfers applied only for single pair and date.");
		}
	}
	
	private void putRequestToCache(String searchId, SearchRequestContainer requestContainer) {
		if (searchId != null) {
			Map<String, Object> params = new HashMap<>();
			params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
			params.put(MemoryCacheHandler.TIME_TO_LIVE, 60000l);
			try {
				cache.write(requestContainer, params);
			} catch (IOCacheException e) {
				LOGGER.error("Error when write searchId: " + searchId, e);
			}
		}
	}
	
	public TripSearchResponse getSearchResult(String searchId) {
		
		// получает запросы поиска с памяти по ид поиска и проверяем их
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		SearchRequestContainer requestContainer = null;
		try {
			requestContainer = (SearchRequestContainer) cache.read(params);
		} catch (ClassCastException | IOCacheException e) {
			LOGGER.error("Empty request by searchId: " + searchId, e);
		}
		if (requestContainer == null) {
			LOGGER.error("Too late for getting result by searchId: " + searchId);
			throw new ApiException(new ResponseError("Too late for getting result or invalid searchId."));
		}
		TripSearchResponse response = service.getSearchResult(searchId);
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		}
		if (response.getResult() != null
				&& !response.getResult().isEmpty()) {
			
			TripSearchResponse result = new TripSearchResponse();
			result.setId(requestContainer.getRequests().get(0).getId().split(";")[0]);
			result.setSearchId(response.getSearchId());
			tripSearchMapping.createDictionaries(result);
			
			TripSearchResponse transfersResult = null; // результат стыковок
			if (isTransfer(requestContainer)) {
				if (requestContainer.getResponse() == null) {
					transfersResult = new TripSearchResponse();
					tripSearchMapping.createDictionaries(transfersResult);
				} else {
					transfersResult = requestContainer.getResponse();
				}
			}
			for (TripSearchResponse searchResponse : response.getResult()) {
				
				Stream<TripSearchRequest> stream = requestContainer.getRequests().stream().filter(r -> r.getId().equals(searchResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					TripSearchRequest request = stream.findFirst().get();
					
					// проверяем ошибки
					if (searchResponse.getTripContainers() != null) {
						
						// логируем ошибки, если есть
						logError(searchResponse);
						prepareResult(request, searchResponse, request.isUseTranfers() ? transfersResult : result);
					}
				}
			}
			if (isTransfer(requestContainer)) {
				
				// сохранение промежуточного результата без очистки неиспользуемых данных
				requestContainer.setResponse(transfersResult);
				
				// создаем сегменты и добавляем в результат
				connectionsController.connectSegments(transfersResult, requestContainer);
				
				// добавляем в кэш запрос под новым searchId, для получения остального результата
				// делаем это перед очисткой данных так как в дальнейшем поиск может еще продолжаться
				// и понадобятся предыдущие результаты для создания стыковок
				putRequestToCache(response.getSearchId(), requestContainer);
				
				// удаляем неиспользуемые данные
				updateResponse(transfersResult, result);
			} else {
				putRequestToCache(response.getSearchId(), requestContainer);
				
				// удаляем неиспользуемые данные
				updateResponse(result, null);
			}
			// меняем ключи мап на ид из мапинга
			tripSearchMapping.updateResultDictionaries(result);
			return result;
		}
		// добавляем в кэш запрос под новым searchId, для получения остального результата
		putRequestToCache(response.getSearchId(), requestContainer);
		return response;
	}
	
	private boolean isTransfer(SearchRequestContainer requestContainer) {
		return requestContainer.getOriginRequest() != null
				&& requestContainer.getOriginRequest().isUseTranfers()
				&& requestContainer.getConnections() != null;
	}
	
	private void prepareResult(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		
		// мапим словари
		tripSearchMapping.mapDictionaries(request, searchResponse, result);
		
		// обновляем мапингом рейсы
		tripSearchMapping.updateSegments(request, searchResponse, result);
		
		// применяем фильтр
		filter.apply(result.getSegments());
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
	
	
	public Route getRoute(String tripId, Lang lang) {
		List<TripDetailsRequest> requests = createTripDetailsRequests(tripId, lang, Method.SEARCH_TRIP_ROUTE, MethodType.GET);
		RouteResponse response = checkResponse(requests.get(0), service.getRoute(requests).get(0));
		
		// мапим пункты маршрута
		Map<String, Locality> localities = new HashMap<>();
		Route route = response.getRoute();
		if (route.getPath() != null) {
			for (RoutePoint point : route.getPath()) {
				if (point.getLocality() != null) {
					if (point.getLocality().getId() != null) {
						localities.put(point.getLocality().getId(), point.getLocality());
					}
					if (point.getLocality().getParent() != null
							&& point.getLocality().getParent().getId() != null) {
						localities.put(point.getLocality().getParent().getId(), point.getLocality().getParent());
					}
				}
			}
			if (!localities.isEmpty()) {
				Map<String, Locality> mapped = new HashMap<>();
				tripSearchMapping.mappingGeo(requests.get(0), localities, mapped);
				long resourceId = Long.parseLong(requests.get(0).getParams().getResource().getId());
				for (RoutePoint point : route.getPath()) {
					if (point.getLocality() != null) {
						Locality parent = point.getLocality().getParent();
						if (mapped.containsKey(tripSearchMapping.getKey(resourceId, point.getLocality().getId()))) {
							point.setLocality(mapped.get(tripSearchMapping.getKey(resourceId, point.getLocality().getId())));
						}
						if (parent != null
								&& mapped.containsKey(tripSearchMapping.getKey(resourceId, parent.getId()))) {
							parent = mapped.get(tripSearchMapping.getKey(resourceId, parent.getId()));
						}
						point.getLocality().setParent(parent);
					}
				}
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
		if (response.getTariffs() != null) {
			response.getTariffs().forEach(t -> tripSearchMapping.applyLang(t, lang)); 
		}
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
		if (response.getConditions() != null) {
			response.getConditions().forEach(c -> tripSearchMapping.applyLang(c, lang)); 
		}
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
				&& !Objects.equals(request.getId(), response.getId())) {
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
			SearchRequestContainer requestContainer = new SearchRequestContainer();
			for (String tripId : tripIds) {
				TripIdModel model = new TripIdModel().create(tripId);
				model.getRequest().setId(StringUtil.generateUUID());
				model.getRequest().setParams(dataController.createResourceParams(model.getResourceId()));
				model.getRequest().setLang(request.getLang());
				model.getRequest().setCurrency(request.getCurrency());
				requestContainer.add(model.getRequest());
			}
			TripSearchResponse response = initSearch(requestContainer);
			
			TripSearchResponse result = new TripSearchResponse();
			tripSearchMapping.createDictionaries(result);
			do {
				// получаем результат сразу же так как он уже в кэше
				response = getSearchResult(response.getSearchId());
				if (response.getSegments() != null) {
						
					// оставляем в запросе только указанный рейс
					response.getSegments().keySet().removeIf(key -> !tripIds.contains(key));
					response.getSegments().values().forEach(segment -> {
						segment.setRoute(null);
						segment.setSeats(null);
					});
					tripSearchMapping.createDictionaries(response);
					updateResponse(response, result);
				}
			} while (response.getSearchId() != null);
			return result;
		} catch (Exception e) {
			LOGGER.error("Error when search selected trips", e);
		}
		return null;
	}
	
	private void updateResponse(TripSearchResponse response, TripSearchResponse result) {
		if (result != null) {
			result.getTripContainers().addAll(response.getTripContainers());
			result.getSegments().putAll(response.getSegments());
			result.getVehicles().putAll(response.getVehicles());
			result.getOrganisations().putAll(response.getOrganisations());
			result.getResources().putAll(response.getResources());
			result.getLocalities().putAll(response.getLocalities());
			response = result;
		}
		Map<String, Segment> segments = response.getSegments();
		Set<String> resultSegmentIds = new HashSet<>(); // ид рейсов в ответе
		if (response.getTripContainers() != null) {
			for (TripContainer container : response.getTripContainers()) {
				if (container.getTrips() != null) {
					for (Trip trip : container.getTrips()) {
						if (!response.getSegments().containsKey(trip.getId())) {
							trip.setId(null);
						} else {
							resultSegmentIds.add(trip.getId());
						}
						if (!response.getSegments().containsKey(trip.getBackId())) {
							trip.setBackId(null);
						} else {
							resultSegmentIds.add(trip.getBackId());
						}
						if (trip.getSegments() != null) {
							if (trip.getSegments().removeIf(id -> !segments.containsKey(id))) {
								trip.setSegments(null);
							} else {
								resultSegmentIds.addAll(trip.getSegments());
							}
						}
					}
					container.getTrips().removeIf(t -> t.getId() == null && t.getBackId() == null && t.getSegments() == null);
				}
			}
		}
		// удаляем лишнее со словарей
		updateResponseDictionaries(resultSegmentIds, segments, response.getVehicles(), response.getOrganisations(), response.getLocalities(), response.getResources());
	}
	
	public void updateResponseDictionaries(Set<String> resultSegmentIds, Map<String, Segment> segments,
			Map<String, Vehicle> vehicles, Map<String, Organisation> organisations, Map<String, Locality> localities,
			Map<String, com.gillsoft.model.Resource> resources) {
		
		// перезаливаем рейсы
		segments.keySet().removeIf(key -> !resultSegmentIds.contains(key));
		
		// перезаливаем словари
		if (vehicles != null) {
			vehicles.keySet().removeIf(key -> {
				for (Segment segment : segments.values()) {
					if (segment.getVehicle() != null
							&& Objects.equals(key, segment.getVehicle().getId())) {
						return false;
					}
				};
				return true;
			});
		}
		if (resources != null) {
			resources.keySet().removeIf(key -> {
				for (Segment segment : segments.values()) {
					if (segment.getResource() != null
							&& Objects.equals(key, segment.getResource().getId())) {
						return false;
					}
				};
				return true;
			});
		}
		if (organisations != null) {
			organisations.keySet().removeIf(key -> {
				for (Segment segment : segments.values()) {
					if ((segment.getCarrier() != null
							&& Objects.equals(key, segment.getCarrier().getId()))
							|| (segment.getInsurance() != null
									&& Objects.equals(key, segment.getInsurance().getId()))) {
						return false;
					}
				};
				return true;
			});
		}
		if (localities != null) {
			localities.keySet().removeIf(key -> {
				for (Segment segment : segments.values()) {
					if (Objects.equals(key, segment.getDeparture().getId())
							|| Objects.equals(key, segment.getArrival().getId())) {
						return false;
					}
					// проверяем маршрут
					if (segment.getRoute() != null
							&& segment.getRoute().getPath() != null) {
						for (RoutePoint point : segment.getRoute().getPath()) {
							if (point.getLocality() != null
									&& Objects.equals(key, point.getLocality().getId())) {
								return false;
							}
						}
					}
				};
				return true;
			});
		}
	}
	
	public OrderResponse search(OrderRequest request) {
		TripSearchResponse search = search(request,
				request.getServices().stream().map(service -> service.getSegment().getId()).collect(Collectors.toSet()));
		OrderResponse response = new OrderResponse();
		response.setId(request.getId());
		response.setCustomers(request.getCustomers());
		response.setServices(new ArrayList<>());
		updateOrderResponse(response, search);
		return response;
	}
	
	private void updateOrderResponse(OrderResponse orderResponse, TripSearchResponse searchResponse) {
		if (searchResponse != null) {
			orderResponse.setVehicles(searchResponse.getVehicles());
			orderResponse.setOrganisations(searchResponse.getOrganisations());
			orderResponse.setLocalities(searchResponse.getLocalities());
			if (orderResponse.getSegments() == null) {
				orderResponse.setSegments(searchResponse.getSegments());
			} else {
				searchResponse.getSegments().forEach((id, s) -> orderResponse.getSegments().putIfAbsent(id, s));
			}
			orderResponse.getSegments().values().forEach(s -> {
				if (s.getResource() != null
						&& searchResponse.getResources().containsKey(s.getResource().getId())) {
					com.gillsoft.model.Resource resource = searchResponse.getResources().get(s.getResource().getId());
					resource.setId(s.getResource().getId());
					s.setResource(resource);
				}
			});
		}
	}
	
	public void mapOrderSegment(OrderRequest orderRequest, OrderResponse orderResponse, OrderResponse result) {
		
		// добавляем рейсы из расписания
		for (String segmentId : orderResponse.getSegments().keySet()) {
			TripSearchResponse segmentResponse = scheduleService.getSegmentResponse(Long.parseLong(orderRequest.getParams().getResource().getId()), segmentId);
			
			// заменяем данными из расписания так как там более подробные данные по рейсу
			if (segmentResponse != null) {
				if (orderResponse.getLocalities() == null) {
					orderResponse.setLocalities(segmentResponse.getLocalities());
				} else if (segmentResponse.getLocalities() != null) {
					orderResponse.getLocalities().putAll(segmentResponse.getLocalities());
				}
				if (orderResponse.getVehicles() == null) {
					orderResponse.setVehicles(segmentResponse.getVehicles());
				} else if (segmentResponse.getVehicles() != null) {
					orderResponse.getVehicles().putAll(segmentResponse.getVehicles());
				}
				if (orderResponse.getOrganisations() == null) {
					orderResponse.setOrganisations(segmentResponse.getOrganisations());
				} else if (segmentResponse.getOrganisations() != null) {
					orderResponse.getOrganisations().putAll(segmentResponse.getOrganisations());
				}
				if (orderResponse.getSegments() == null) {
					orderResponse.setSegments(segmentResponse.getSegments());
				} else if (segmentResponse.getSegments() != null) {
					orderResponse.getSegments().putAll(segmentResponse.getSegments());
				}
			}
		}
		TripSearchResponse searchResponse = new TripSearchResponse();
		searchResponse.setLocalities(orderResponse.getLocalities());
		searchResponse.setVehicles(orderResponse.getVehicles());
		searchResponse.setOrganisations(orderResponse.getOrganisations());
		searchResponse.setSegments(orderResponse.getSegments());
		
		// выполняем маппинг данных
		mapOrderSegment(orderRequest, orderResponse, result, searchResponse);
	}
	
	private void mapOrderSegment(OrderRequest orderRequest, OrderResponse orderResponse, OrderResponse result, TripSearchResponse searchResponse) {
		TripSearchRequest request = new TripSearchRequest();
		request.setCurrency(orderRequest.getCurrency());
		request.setLang(orderRequest.getLang());
		request.setParams(orderRequest.getParams());
		
		TripSearchResponse searchResult = new TripSearchResponse();
		tripSearchMapping.createDictionaries(searchResult);
		
		// мапим словари
		tripSearchMapping.mapDictionaries(request, searchResponse, searchResult);
		
		// обновляем мапингом рейсы
		tripSearchMapping.updateSegments(request, searchResponse, searchResult, false);
		
		updateOrderResponse(orderResponse, searchResult);
		
		if (result.getVehicles() == null) {
			result.setVehicles(orderResponse.getVehicles());
		} else {
			orderResponse.getVehicles().forEach((id, v) -> result.getVehicles().putIfAbsent(id, v));
		}
		if (result.getOrganisations() == null) {
			result.setOrganisations(orderResponse.getOrganisations());
		} else {
			orderResponse.getOrganisations().forEach((id, o) -> result.getOrganisations().putIfAbsent(id, o));
		}
		if (result.getLocalities() == null) {
			result.setLocalities(orderResponse.getLocalities());
		} else {
			orderResponse.getLocalities().forEach((id, l) -> result.getLocalities().putIfAbsent(id, l));
		}
		if (result.getSegments() == null) {
			result.setSegments(orderResponse.getSegments());
		} else {
			orderResponse.getSegments().forEach((id, s) -> result.getSegments().putIfAbsent(id, s));
		}
	}

}
