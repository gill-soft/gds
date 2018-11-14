package com.gillsoft.service.impl;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.config.Config;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.ResourceMethodResponse;
import com.gillsoft.model.response.ResourceResponse;
import com.gillsoft.service.AgregatorResourceInfoService;

@Service
public class AgregatorResourceInfoRestService extends AbstractAgregatorRestService
		implements AgregatorResourceInfoService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorResourceInfoRestService.class);
	
	private static final String INFO = "resource";
	
	private static final String METHODS = "resource/method";

	@Override
	public List<ResourceResponse> getInfo(List<ResourceRequest> request) {
		return getResult(request, new ParameterizedTypeReference<List<ResourceResponse>>() { }, INFO);
	}

	@Override
	public List<ResourceMethodResponse> getAvailableMethods(List<ResourceRequest> request) {
		return getResult(request, new ParameterizedTypeReference<List<ResourceMethodResponse>>() { }, METHODS);
	}
	
	private <T> T getResult(List<ResourceRequest> request, ParameterizedTypeReference<T> type, String method) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + method).build().toUri();
		RequestEntity<List<ResourceRequest>> entity = new RequestEntity<List<ResourceRequest>>(request, HttpMethod.POST, uri);
		try {
			return getResult(entity, type);
		} catch (ResponseError e) {
			return null;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
