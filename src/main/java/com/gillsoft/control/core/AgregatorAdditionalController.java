package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.control.service.AgregatorAdditionalSearchService;
import com.gillsoft.model.request.AdditionalDetailsRequest;
import com.gillsoft.model.request.AdditionalSearchRequest;
import com.gillsoft.model.response.AdditionalSearchResponse;
import com.gillsoft.model.response.DocumentsResponse;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.TariffsResponse;
import com.gillsoft.util.CacheUtil;
import com.gillsoft.util.StringUtil;

@Component
public class AgregatorAdditionalController {
	
	@Autowired
	private List<AgregatorAdditionalSearchService> agregatorAdditionalServices;
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	public AdditionalSearchResponse initSearch(List<AdditionalSearchRequest> request) {
		// список ид, по которым будем получать результат
		List<String> resultIds = new ArrayList<>();
		for (AgregatorAdditionalSearchService service : agregatorAdditionalServices) {
			String cacheId = StringUtil.generateUUID() + ";" + service.getClass().getName();
			resultIds.add(cacheId);
			ThreadPoolStore.execute(PoolType.SEARCH, () -> {
				// добавляем в кэш результат под указанным ид
				CacheUtil.putToCache(cache, cacheId, service.initSearch(request));
			});
		}
		// под ид поиска ложим в кэш список ид, по которым будем получать результат
		String searchId = StringUtil.generateUUID();
		CacheUtil.putToCache(cache, searchId, resultIds);
		return new AdditionalSearchResponse(null, searchId);
	}

	@SuppressWarnings("unchecked")
	public AdditionalSearchResponse getSearchResult(String searchId) {
		try {
			// вытаскиваем с кэша ресурсы, по которым нужно получить результат поиска
			List<String> resultIds = (List<String>) CacheUtil.getFromCache(cache, searchId);
			if (resultIds == null) {
				throw new IOCacheException("Too late for getting result");
			}
			// список ид, по которым будем дополучать результат
			List<String> nextResultIds = new ArrayList<>();
			
			// берем ответы по каждому поиску
			Map<String, AdditionalSearchResponse> searchResponses = new HashMap<>();
			for (String cacheId : resultIds) {
				AdditionalSearchResponse searchResponse = (AdditionalSearchResponse) CacheUtil.getFromCache(cache, cacheId);
				if (searchResponse != null) {
					searchResponses.put(cacheId, searchResponse);
				} else {
					
					// если результата нет, то он еще в процессе, так что добавляем старый ид для добора результата
					nextResultIds.add(cacheId);
				}
			}
			nextResultIds.addAll(initSearchResult(searchResponses));
			
			// создаем ссылку на следующую часть результата поиска или завершаем его
			AdditionalSearchResponse response = null;
			if (!nextResultIds.isEmpty()) {
				String nextSearchId = StringUtil.generateUUID();
				CacheUtil.putToCache(cache, nextSearchId, nextResultIds);
				response = new AdditionalSearchResponse(null, nextSearchId);
			} else {
				response = new AdditionalSearchResponse();
			}
			response.setResult(new ArrayList<>());
			for (AdditionalSearchResponse searchResponse : searchResponses.values()) {
				if (searchResponse.getResult() != null) {
					response.getResult().addAll(searchResponse.getResult());
				}
			}
			return response;
		} catch (IOCacheException e) {
			return new AdditionalSearchResponse(null, e);
		}
	}
	
	private List<String> initSearchResult(Map<String, AdditionalSearchResponse> searchResponses) {
		
		// список ид, по которым будем дополучать результат
		List<String> resultIds = new ArrayList<>();
		
		// запускаем получение результата поиска
		for (Entry<String, AdditionalSearchResponse> searchResponseEntry : searchResponses.entrySet()) {
			if (searchResponseEntry.getValue().getSearchId() != null) {
				for (AgregatorAdditionalSearchService service : agregatorAdditionalServices) {
					if (searchResponseEntry.getKey().contains(";" + service.getClass().getName())) {
						String cacheId = StringUtil.generateUUID() + ";" + service.getClass().getName();
						resultIds.add(cacheId);
						ThreadPoolStore.execute(PoolType.SEARCH, () -> {
							// добавляем в кэш результат под указанным ид
							// формируем новый ответ с новым ид поиска
							CacheUtil.putToCache(cache, cacheId, service.getSearchResult(searchResponseEntry.getValue().getSearchId()));
						});
						break;
					}
				}
			}
		}
		return resultIds;
	}

	public List<TariffsResponse> getTariffs(List<AdditionalDetailsRequest> requests) {
		return getResult((service, serviceRequests) -> service.getTariffs(requests), requests);
	}

	public List<RequiredResponse> getRequiredFields(List<AdditionalDetailsRequest> requests) {
		return getResult((service, serviceRequests) -> service.getRequiredFields(requests), requests);
	}

	public List<ReturnConditionResponse> getConditions(List<AdditionalDetailsRequest> requests) {
		return getResult((service, serviceRequests) -> service.getConditions(requests), requests);
	}

	public List<DocumentsResponse> getDocuments(List<AdditionalDetailsRequest> requests) {
		return getResult((service, serviceRequests) -> service.getDocuments(requests), requests);
	}
	
	public <T> List<T> getResult(BiFunction<AgregatorAdditionalSearchService, List<AdditionalDetailsRequest>, List<T>> responseCreator, List<AdditionalDetailsRequest> requests) {
		List<Callable<List<T>>> callables = new ArrayList<>(agregatorAdditionalServices.size());
		for (AgregatorAdditionalSearchService service : agregatorAdditionalServices) {
			callables.add(() -> {
				return responseCreator.apply(service, requests);
			});
		}
		return ThreadPoolStore.getResult(PoolType.SEARCH, callables).stream().filter(Objects::nonNull).flatMap(l -> l.stream()).collect(Collectors.toList());
	}

}
