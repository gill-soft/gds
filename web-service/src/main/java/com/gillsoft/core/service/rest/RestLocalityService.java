package com.gillsoft.core.service.rest;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.service.LocalityService;

//TODO добавить обработку статусов ответов ResponseEntity
public class RestLocalityService implements LocalityService {
	
	private RestResourceService resourceService;
	
	@Override
	public List<Locality> getAll(LocalityRequest request) {
		return getResult(request, Method.LOCALITY_ALL, new ParameterizedTypeReference<List<Locality>>() { });
	}

	@Override
	public List<Locality> getUsed(LocalityRequest request) {
		return getResult(request, Method.LOCALITY_USED, new ParameterizedTypeReference<List<Locality>>() { });
	}

	@Override
	public Map<String, List<String>> getBinding(LocalityRequest request) {
		return getResult(request, Method.LOCALITY_BINDING, new ParameterizedTypeReference<Map<String, List<String>>>() { });
	}
	
	private <T> T getResult(LocalityRequest request, String method, ParameterizedTypeReference<T> type) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + method)
				.build().toUri();
		RequestEntity<LocalityRequest> requestEntity = new RequestEntity<LocalityRequest>(request, HttpMethod.POST, uri);
		ResponseEntity<T> response = resourceService.getTemplate().exchange(
				requestEntity, type);
		return response.getBody();
	}

	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}

}
