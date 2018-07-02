package com.gillsoft.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.core.store.ResourceStore;
import com.gillsoft.model.request.SeatsRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.SeatsResponse;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TripSearchController {
	
	@Autowired
	private ResourceStore store;
	
	@Autowired
	private ResourceActivity activity;
	
	@Autowired
	@Qualifier("MemoryCacheHandler")
	private CacheHandler cache;
	
	public TripSearchResponse initSearch(List<TripSearchRequest> requests) {
		
		// создаем поиски
		List<Callable<TripSearchResponse>> callables = new ArrayList<>();
		for (final TripSearchRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new TripSearchResponse(request.getId(), store.getResourceService(
							request.getParams()).getSearchService().initSearch(request).getSearchId(), request);
				} catch (Exception e) {
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
			if (searchResponses == null) {
				throw new IOCacheException("Too late for getting result");
			}
			List<Callable<TripSearchResponse>> callables = initSearchResult(searchResponses);
			
			// создаем ссылку на следующую часть результата поиска или завершаем его
			TripSearchResponse response = null;
			if (!callables.isEmpty()) {
				
				// проверяем выполнились какие-нибудь задания
				searchResponses = ThreadPoolStore.getResult(PoolType.SEARCH, callables);
				
				response = isPresentNextResult(searchResponses) ? putToCache(searchResponses) : new TripSearchResponse();
			} else {
				response = new TripSearchResponse();
			}
			response.setResult(new ArrayList<>());
			for (TripSearchResponse searchResponse : searchResponses) {
				
				// в результат нужно добавить только ид запроса и список рейсов
				response.getResult().add(createCopy(searchResponse));
			}
			return response;
		} catch (IOCacheException e) {
			return new TripSearchResponse(null, e);
		}
	}
	
	/*
	 * проверяет есть ли ссылка на следующий результат
	 */
	private boolean isPresentNextResult(List<TripSearchResponse> searchResponses) {
		for (TripSearchResponse tripSearchResponse : searchResponses) {
			if (tripSearchResponse.getSearchId() != null) {
				return true;
			}
		}
		return false;
	}
	
	private List<Callable<TripSearchResponse>> initSearchResult(List<TripSearchResponse> searchResponses) {
		List<Callable<TripSearchResponse>> callables = new ArrayList<>();
		
		// запускаем получение результата поиска
		for (final TripSearchResponse searchResponse : searchResponses) {

			// берем из ответа ид поиска и, если он есть, то отправляем запрос на связанный с ид поиска ресурс
			if (searchResponse.getSearchId() != null
					&& searchResponse.getRequest() != null) {
				final TripSearchRequest request = searchResponse.getRequest();
				callables.add(() -> {
					try {
						activity.check(request);
						
						// формируем новый ответ с новым ид поиска
						TripSearchResponse response = store.getResourceService(
								request.getParams()).getSearchService().getSearchResult(searchResponse.getSearchId());
						response.setId(request.getId());
						response.setRequest(request);
						return response;
					} catch (Exception e) {
						return new TripSearchResponse(request.getId(), e);
					}
				});
			}
		}
		return callables;
	}
	
	private TripSearchResponse createCopy(TripSearchResponse searchResponse) {
		TripSearchResponse copy = new TripSearchResponse();
		copy.setId(searchResponse.getId());
		copy.setLocalities(searchResponse.getLocalities());
		copy.setOrganisations(searchResponse.getOrganisations());
		copy.setVehicles(searchResponse.getVehicles());
		copy.setSegments(searchResponse.getSegments());
		copy.setTripContainers(searchResponse.getTripContainers());
		return copy;
	}
	
	private TripSearchResponse putToCache(List<TripSearchResponse> searchResponses) {
		String searchId = StringUtil.generateUUID();
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		params.put(MemoryCacheHandler.TIME_TO_LIVE, 60000l);
		try {
			cache.write(searchResponses, params);
			return new TripSearchResponse(null, searchId);
		} catch (IOCacheException e) {
			return new TripSearchResponse(null, e);
		}
	}
	
	public List<SeatsResponse> getSeats(List<SeatsRequest> requests) {
		List<Callable<SeatsResponse>> callables = new ArrayList<>();
		for (final SeatsRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new SeatsResponse(request.getId(),
							store.getResourceService(request.getParams()).getSearchService().getSeats(request.getTripId()));
				} catch (Exception e) {
					return new SeatsResponse(request.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.SEARCH, callables);
	}

}
