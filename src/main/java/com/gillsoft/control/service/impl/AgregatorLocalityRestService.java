package com.gillsoft.control.service.impl;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.AgregatorLocalityService;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;

@Service
public class AgregatorLocalityRestService extends AbstractAgregatorRestService implements AgregatorLocalityService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorLocalityRestService.class);
	
	private static final String ALL = "locality/all";
	
	private static final String USED = "locality/used";
	
	private static final String BINDING = "locality/binding";

	@Override
	public List<LocalityResponse> getAll(List<LocalityRequest> request) {
		return getResult(request, ALL);
	}

	@Override
	public List<LocalityResponse> getUsed(List<LocalityRequest> request) {
		return getResult(request, USED);
	}

	@Override
	public List<LocalityResponse> getBinding(List<LocalityRequest> request) {
		return getResult(request, BINDING);
	}
	
	private List<LocalityResponse> getResult(List<LocalityRequest> request, String method) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + method).build().toUri();
		RequestEntity<List<LocalityRequest>> entity = new RequestEntity<List<LocalityRequest>>(request, HttpMethod.POST, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<List<LocalityResponse>>() { });
		} catch (ResponseError e) {
			return null;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
