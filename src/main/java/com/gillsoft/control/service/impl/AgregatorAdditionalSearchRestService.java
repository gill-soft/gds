package com.gillsoft.control.service.impl;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.core.Utils;
import com.gillsoft.control.service.AgregatorAdditionalSearchService;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.request.AdditionalDetailsRequest;
import com.gillsoft.model.request.AdditionalSearchRequest;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.AdditionalSearchResponse;
import com.gillsoft.model.response.DocumentsResponse;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.TariffsResponse;

@Service
public class AgregatorAdditionalSearchRestService extends AbstractAgregatorRestService implements AgregatorAdditionalSearchService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorAdditionalSearchRestService.class);
	
	private static final String SEARCH = "additional";
	
	private static final String TARIFFS = "additional/tariffs";
	
	private static final String REQUIRED = "additional/required";
	
	private static final String CONDITIONS = "additional/conditions";
	
	private static final String DOCUMENTS = "additional/documents";
	
	@Override
	public AdditionalSearchResponse initSearch(List<AdditionalSearchRequest> request) {
		try {
			return getResult(request, new ParameterizedTypeReference<AdditionalSearchResponse>() { }, SEARCH);
		} catch (ResponseError e) {
			return new AdditionalSearchResponse(null, e);
		}
	}

	@Override
	public AdditionalSearchResponse getSearchResult(String searchId) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + SEARCH)
				.queryParam("searchId", searchId).build().toUri();
		RequestEntity<Object> entity = new RequestEntity<Object>(HttpMethod.GET, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<AdditionalSearchResponse>() { });
		} catch (ResponseError e) {
			return new AdditionalSearchResponse(null, e);
		}
	}

	@Override
	public List<TariffsResponse> getTariffs(List<AdditionalDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<TariffsResponse>>() { }, TARIFFS);
		} catch (ResponseError e) {
			return Collections.singletonList(new TariffsResponse(null, e));
		}
	}

	@Override
	public List<RequiredResponse> getRequiredFields(List<AdditionalDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<RequiredResponse>>() { }, REQUIRED);
		} catch (ResponseError e) {
			return Collections.singletonList(new RequiredResponse(null, e));
		}
	}

	@Override
	public List<ReturnConditionResponse> getConditions(List<AdditionalDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<ReturnConditionResponse>>() { }, CONDITIONS);
		} catch (ResponseError e) {
			return Collections.singletonList(new ReturnConditionResponse(null, e));
		}
	}

	@Override
	public List<DocumentsResponse> getDocuments(List<AdditionalDetailsRequest> requests) {
		try {
			return getResult(requests, new ParameterizedTypeReference<List<DocumentsResponse>>() { }, DOCUMENTS);
		} catch (ResponseError e) {
			return Collections.singletonList(new DocumentsResponse(null, e));
		}
	}
	
	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
	
	private <T> T getResult(List<? extends ResourceRequest> request, ParameterizedTypeReference<T> type, String method) throws ResponseError {
		return getResult(request, type, method, HttpMethod.POST);
	}
	
	private <T> T getResult(List<? extends ResourceRequest> request, ParameterizedTypeReference<T> type, String method, HttpMethod httpMethod) throws ResponseError {
		if (request.stream().noneMatch(r -> Utils.isPresentHost(r))) {
			return null;
		}
		request = request.stream().filter(r -> Utils.isPresentHost(r)).collect(Collectors.toList());
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + method).build().toUri();
		RequestEntity<List<? extends ResourceRequest>> entity = new RequestEntity<List<? extends ResourceRequest>>(request, httpMethod, uri);
		return getResult(entity, type);
	}
	
}
