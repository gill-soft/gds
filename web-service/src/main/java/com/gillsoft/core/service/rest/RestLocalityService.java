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
		return getResult(request, Method.LOCALITY_ALL);
	}

	@Override
	public List<Locality> getUsed(LocalityRequest request) {
		return getResult(request, Method.LOCALITY_USED);
	}

	@Override
	public Map<String, List<String>> getBinding(LocalityRequest request) {
		return getResult(request, Method.LOCALITY_BINDING);
	}
	
	private <T> T getResult(LocalityRequest request, String method) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.LOCALITY_BINDING)
				.build().toUri();
		RequestEntity<LocalityRequest> requestEntity = new RequestEntity<LocalityRequest>(request, HttpMethod.POST, uri);
		ResponseEntity<T> response = resourceService.getTemplate().exchange(
				requestEntity, new ParameterizedTypeReference<T>() { });
		return response.getBody();
	}

	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}

}
