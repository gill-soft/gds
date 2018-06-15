package com.gillsoft.core;

import java.rmi.AccessException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.core.store.ResourceStore;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TripSearchController {
	
	@Autowired
	private ResourceStore store;
	
	@Autowired
	private ResourceActivity activity;
	
	@Autowired
	private CacheHandler cache;
	
	public TripSearchResponse initSearch(List<TripSearchRequest> requests) {
		
		// создаем поиски
		List<Callable<TripSearchResponse>> callables = new ArrayList<>();
		for (final TripSearchRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new TripSearchResponse(request.getId(),
							store.getResourceService(request.getParams()).getSearchService().initSearch(request), request);
				} catch (AccessException e) {
					return new TripSearchResponse(request.getId(), e);
				}
			});
		}
		// под ид поиска ложим в кэш запросы, по которым будем получать результат
		return putToCache(ThreadPoolStore.getResult(PoolType.SEARCH, callables));
	}
	
	public TripSearchResponse getSearchResult(String searchId) {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		try {
			// вытаскиваем с кэша ресурсы, по которым нужно получить результат поиска
			@SuppressWarnings("unchecked")
			List<TripSearchResponse> searchResponses = (List<TripSearchResponse>) cache.read(params);
			List<Callable<TripSearchResponse>> callables = new ArrayList<>();
			
			// запускаем получение результата поиска
			for (final TripSearchResponse searchResponse : searchResponses) {

				// берем из ответа ид поиска и, если он есть, то отправляем запрос на связанный м ид поиска ресурс
				if (searchResponse.getSearchId() != null
						&& searchResponse.getRequest() != null) {
					final TripSearchRequest request = searchResponse.getRequest();
					callables.add(() -> {
						try {
							activity.check(request);
							
							// формируем новый ответ с новым ид поиска
							TripSearchResponse response = store.getResourceService(
									request.getParams()).getSearchService().getSearchResult(searchResponse.getSearchId());
							return new TripSearchResponse(request.getId(), response.getSearchId(), response.getTrips(), request);
						} catch (AccessException e) {
							return new TripSearchResponse(request.getId(), e);
						}
					});
				}
			}
			// создаем ссылку на следующую часть результата поиска
			TripSearchResponse response = putToCache(ThreadPoolStore.getResult(PoolType.SEARCH, callables));
			response.setResult(new ArrayList<>());
			for (TripSearchResponse searchResponse : searchResponses) {
				
				// в результат нужно добавить только ид запроса и список рейсов
				TripSearchResponse copy = new TripSearchResponse();
				copy.setId(searchResponse.getId());
				copy.setTrips(searchResponse.getTrips());
				response.getResult().add(copy);
			}
			return response;
		} catch (IOCacheException e) {
			return new TripSearchResponse(null, e);
		}
	}
	
	private TripSearchResponse putToCache(List<TripSearchResponse> searchResponses) {
		String searchId = UUID.randomUUID().toString();
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		params.put(MemoryCacheHandler.TIME_TO_LIVE, 60000);
		try {
			cache.write(searchResponses, params);
			return new TripSearchResponse(null, searchId);
		} catch (IOCacheException e) {
			return new TripSearchResponse(null, e);
		}
	}

}
