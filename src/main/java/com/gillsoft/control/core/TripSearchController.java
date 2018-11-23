package com.gillsoft.control.core;

import java.util.ArrayList;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.AgregatorTripSearchService;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.TripContainer;
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
	private MappingService mappingService;
	
	@Autowired
	private TripSearchMapping tripSearchMapping;
	
	public TripSearchResponse initSearch(TripSearchRequest request) {
		
		// проверяем параметры запроса TODO
		
		// проверяем ответ и записываем в память запросы под ид поиска
		List<TripSearchRequest> requests = createSearchRequest(request);
		TripSearchResponse response = service.initSearch(requests);
		if (response.getError() != null) {
			return response;
		}
		putRequestToCache(response.getSearchId(), requests);
		return response;
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
			return new TripSearchResponse(null, new ResponseError("Too late for getting result or invalid searchId."));
		}
		TripSearchResponse response = service.getSearchResult(searchId);
		if (response.getError() != null) {
			return response;
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
				
				Stream<TripSearchRequest> stream = requests.stream().filter(request -> request.getId().equals(searchResponse.getId()));
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
			return request;
		}
		return null;
	}

}
