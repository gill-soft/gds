package com.gillsoft.control.core;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
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
import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.core.data.DataConverter;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.core.mapping.MappingCreator;
import com.gillsoft.control.core.mapping.TripSearchMapping;
import com.gillsoft.control.core.request.SearchRequestController;
import com.gillsoft.model.AdditionalServiceItem;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.request.AdditionalDetailsRequest;
import com.gillsoft.model.request.AdditionalSearchRequest;
import com.gillsoft.model.response.AdditionalSearchResponse;
import com.gillsoft.model.response.DocumentsResponse;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.TariffsResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.CacheUtil;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AdditionalSearchController {
	
	private static Logger LOGGER = LogManager.getLogger(AdditionalSearchController.class);
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	@Autowired
	private AgregatorAdditionalController additionalController;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private SearchRequestController requestController;
	
	@Autowired
	private TripSearchMapping tripSearchMapping;
	
	/**
	 * Запускает поиск по запросу с АПИ. Конвертирует обобщенный запрос в запросы ко всем доступным ресурсам.
	 */
	public AdditionalSearchResponse initSearch(AdditionalSearchRequest request) {
		
		// проверяем параметры запроса
		validateSearchRequest(request);
		
		List<AdditionalSearchRequest> requests = requestController.createSearchRequest(request);
		
		// проверяем ответ и записываем в память запросы под ид поиска
		AdditionalSearchResponse response = infoController.checkResponse(null, additionalController.initSearch(requests));
		response.setId(request.getId());
		putRequestToCache(response.getSearchId(), requests);
		return response;
	}
	
	private void validateSearchRequest(AdditionalSearchRequest request) {
		if (request.getOrder() == null
				|| request.getOrder().getOrderId() == null
				&& (request.getSegments() == null
						|| request.getSegments().isEmpty()
						|| request.getSegments().stream().allMatch(s -> s.getId() == null || s.getId().isEmpty()))) {
			throw new RequestValidateException("Empty order or segments");
		}
		if (request.getOrder() != null
				&& request.getOrder().getOrderId() != null) {
			try {
				Long.parseLong(request.getOrder().getOrderId());
			} catch (NumberFormatException e) {
				throw new RequestValidateException("Order id must be a number");
			}
		}
	}
	
	private void putRequestToCache(String searchId, List<AdditionalSearchRequest> requests) {
		if (searchId != null) {
			CacheUtil.putToCache(cache, searchId, requests);
		}
	}
	
	@SuppressWarnings("unchecked")
	public AdditionalSearchResponse getSearchResult(String searchId) {
		
		// получает запросы поиска с памяти по ид поиска и проверяем их
		List<AdditionalSearchRequest> requests = null;
		try {
			requests = (List<AdditionalSearchRequest>) CacheUtil.getFromCache(cache, searchId);
		} catch (ClassCastException e) {
			LOGGER.error("Empty request by searchId: " + searchId, e);
		}
		if (requests == null) {
			LOGGER.error("Too late for getting result by searchId: " + searchId);
			throw new ApiException(new ResponseError("Too late for getting result or invalid searchId."));
		}
		AdditionalSearchResponse response = additionalController.getSearchResult(searchId);
		if (response.getError() != null) {
			throw new ApiException(response.getError());
		}
		response.setId(requests.get(0).getId().split(";")[0]);
		if (response.getResult() != null
				&& !response.getResult().isEmpty()) {
			AdditionalSearchResponse result = new AdditionalSearchResponse();
			result.setId(response.getId());
			result.setSearchId(response.getSearchId());
			tripSearchMapping.createDictionaries(result);
			List<AdditionalSearchResponse> responses = response.getResult().stream().map(r -> (AdditionalSearchResponse) r).collect(Collectors.toList());
			for (AdditionalSearchResponse searchResponse : responses) {
				Stream<AdditionalSearchRequest> stream = requests.stream().filter(r -> r.getId().equals(searchResponse.getId()));
				if (stream != null) {
					Optional<AdditionalSearchRequest> optional = stream.findFirst();
					if (optional.isPresent()) {
						
						// запрос, по которому получен результат
						AdditionalSearchRequest request = optional.get();
						request.setSearchCompleted(searchResponse.getSearchId() == null);
						
						// проверяем ошибки
						if (searchResponse.getAdditionalServices() != null) {
							
							// логируем ошибки, если есть
							logError(searchResponse);
							prepareResult(request, searchResponse, result);
						}
					}
				}
			}
			// удаляем неиспользуемые данные
			result.fillMaps();
			
			// добавляем в кэш запрос под новым searchId, для получения остального результата
			putRequestToCache(result.getSearchId(), requests);
			return result;
		}
		// добавляем в кэш запрос под новым searchId, для получения остального результата
		putRequestToCache(response.getSearchId(), requests);
		return response;
	}
	
	private void logError(AdditionalSearchResponse searchResponse) {
		if (searchResponse.getError() != null) {
			try {
				LOGGER.error("ERROR in response id: " + searchResponse.getId()
				+ "\n" + StringUtil.objectToJsonString(searchResponse.getError()));
			} catch (JsonProcessingException e) {
				LOGGER.error("ERROR in response id: " + searchResponse.getError());
			}
		}
	}
	
	private void prepareResult(AdditionalSearchRequest request, AdditionalSearchResponse searchResponse, AdditionalSearchResponse result) {
		long resourceId = MappingCreator.getResourceId(request);
		result.getResources().put(String.valueOf(resourceId), request.getParams().getResource());
		for (Entry<String, AdditionalServiceItem> entry : searchResponse.getAdditionalServices().entrySet()) {
			AdditionalServiceItem service = entry.getValue();
			
			// устанавливаем ресурс
			service.setResource(new com.gillsoft.model.Resource(String.valueOf(resourceId)));
			
			// начисление сборов
			try {
				service.setPrice(dataController.recalculate(service.getPrice(), request.getCurrency()));
				DataConverter.applyLang(service.getPrice().getTariff(), request.getLang());
			} catch (Exception e) {
				continue;
			}
			// добавляем рейсы в результат
			result.getAdditionalServices().put(new IdModel(resourceId, entry.getKey()).asString(), service);
		}
	}
	
	public List<Tariff> getTariffs(String serviceAdditionalId, Lang lang) {
		List<AdditionalDetailsRequest> requests = createAdditionalDetailsRequests(serviceAdditionalId, lang, Method.ADDITIONAL_TARIFFS, MethodType.GET);
		TariffsResponse response = infoController.checkResponse(requests.get(0), additionalController.getTariffs(requests).get(0));
		if (response.getTariffs() != null) {
			response.getTariffs().forEach(t -> DataConverter.applyLang(t, lang)); 
		}
		return response.getTariffs();
	}

	public List<RequiredField> getRequiredFields(String serviceAdditionalId) {
		List<AdditionalDetailsRequest> requests = createAdditionalDetailsRequests(serviceAdditionalId, null, Method.ADDITIONAL_REQUIRED, MethodType.GET);
		RequiredResponse response = infoController.checkResponse(requests.get(0), additionalController.getRequiredFields(requests).get(0));
		return response.getFields();
	}
	
	public List<ReturnCondition> getConditions(String serviceAdditionalId, String tariffId, Lang lang) {
		List<AdditionalDetailsRequest> requests = createAdditionalDetailsRequests(serviceAdditionalId, lang, Method.ADDITIONAL_CONDITIONS, MethodType.GET);
		requests.get(0).setTariffId(tariffId);
		ReturnConditionResponse response = infoController.checkResponse(requests.get(0), additionalController.getConditions(requests).get(0));
		if (response.getConditions() != null) {
			response.getConditions().forEach(c -> DataConverter.applyLang(c, lang)); 
		}
		return response.getConditions();
	}
	
	public List<Document> getDocuments(String serviceAdditionalId, Lang lang) {
		List<AdditionalDetailsRequest> requests = createAdditionalDetailsRequests(serviceAdditionalId, lang, Method.ADDITIONAL_DOCUMENTS, MethodType.GET);
		DocumentsResponse response = infoController.checkResponse(requests.get(0), additionalController.getDocuments(requests).get(0));
		return response.getDocuments();
	}
	
	public List<AdditionalDetailsRequest> createAdditionalDetailsRequests(String serviceAdditionalId, Lang lang, String methodPath, MethodType methodType) {
		return Collections.singletonList(createAdditionalDetailsRequest(serviceAdditionalId, lang, methodPath, methodType));
	}
	
	public AdditionalDetailsRequest createAdditionalDetailsRequest(String serviceAdditionalId, Lang lang, String methodPath, MethodType methodType) {
		IdModel idModel = new IdModel().create(serviceAdditionalId);
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			for (Resource resource : resources) {
				if (resource.getId() == idModel.getResourceId()) {
					if (infoController.isMethodAvailable(resource, methodPath, methodType)) {
						AdditionalDetailsRequest request = new AdditionalDetailsRequest();
						request.setId(StringUtil.generateUUID());
						request.setLang(lang);
						request.setParams(resource.createParams());
						request.setAdditionalServiceId(idModel.getId());
						return request;
					} else {
						throw new MethodUnavalaibleException("Method for this service is unavailable");
					}
				}
			}
		}
		throw new ResourceUnavailableException("Resource of this trip is unavailable for user");
	}

}
