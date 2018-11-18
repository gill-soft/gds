package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.AgregatorTripSearchService;
import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;

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
	private LocalityController localityController;
	
	@Autowired
	private MappingService mappingService;
	
	public TripSearchResponse initSearch(TripSearchRequest request) {
		
		// проверяем ответ и записываем в память запросы под ид поиска
		List<TripSearchRequest> requests = createSearchRequest(request);
		TripSearchResponse response = service.initSearch(requests);
		if (response.getError() != null) {
			return response;
		}
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, response.getSearchId());
		params.put(MemoryCacheHandler.TIME_TO_LIVE, 60000l);
		try {
			cache.write(requests, params);
		} catch (IOCacheException e) {
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public TripSearchResponse getSearchResult(String searchId) {
		
		//
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		List<TripSearchRequest> requests = null;
		try {
			Object value = cache.read(params);
			requests = (List<TripSearchRequest>) value;
		} catch (IOCacheException | ClassCastException e) {
			LOGGER.error("Too late for getting result by searchId: " + searchId, e);
			return new TripSearchResponse(null, new ResponseError("Too late for getting result by searchId: " + searchId));
		}
		if (requests == null) {
			LOGGER.error("Empty cache value by searchId: " + searchId);
			return new TripSearchResponse(null, new ResponseError("Invalid searchId."));
		}
		TripSearchResponse response = service.getSearchResult(searchId);
		if (response.getError() != null) {
			return response;
		}
		if (response.getResult() != null
				&& !response.getResult().isEmpty()) {
			
			TripSearchResponse result = new TripSearchResponse();
			
			Map<String, Locality> localities = new HashMap<>();
			result.setLocalities(localities);
			
			Map<String, Organisation> organisations = new HashMap<>();
			result.setOrganisations(organisations);
			
			Map<String, Vehicle> vehicles = new HashMap<>();
			result.setVehicles(vehicles);
			
			for (TripSearchResponse searchResponse : response.getResult()) {
				
				Stream<TripSearchRequest> stream = requests.stream().filter(request -> request.getId().equals(searchResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					TripSearchRequest request = stream.findFirst().get();
					
					// мапим пункты ответа
					mappingObjects(request, searchResponse.getLocalities(), localities, MapType.GEO,
							(mapping, lang) -> localityController.createLocality(mapping, lang));
					
					// мапим организации
					mappingObjects(request, searchResponse.getOrganisations(), organisations, MapType.CARRIER,
							(mapping, lang) -> createOrganisation(mapping, lang));
					
					// мапим транспорт
					mappingObjects(request, searchResponse.getVehicles(), vehicles, MapType.BUS,
							(mapping, lang) -> createVehicle(mapping, lang));
				}
				
			}
		}
		return response;
	}
	
	private <T> void mappingObjects(TripSearchRequest request, Map<String, T> objects, Map<String, T> result, MapType mapType, MapObjectCreator<T> creator) {
		for (Entry<String, T> object : objects.entrySet()) {
			
			// получаем смапленную сущность
			List<Mapping> mappings = mappingService.getMappings(mapType,
					request.getParams().getResource().getId(), object.getKey(), request.getLang());
			if (mappings == null
					|| !mappings.isEmpty()) {
				result.put(object.getKey(), object.getValue());
			} else {
				result.put(object.getKey(), creator.create(mappings.get(0), request.getLang()));
			}
		}
	}
	
	private Organisation createOrganisation(Mapping mapping, Lang lang) {
		return null; //TODO create organisation from mapping
	}
	
	private Vehicle createVehicle(Mapping mapping, Lang lang) {
		return null; //TODO create vehicle from mapping
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
								resourceSearchRequest.setId(StringUtil.generateUUID());
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
			return request;
		}
		return null;
	}
	
	private interface MapObjectCreator<T> {
		
		public T create(Mapping mapping, Lang lang);
		
	}

}
