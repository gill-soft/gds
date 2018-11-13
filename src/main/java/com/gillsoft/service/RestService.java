package com.gillsoft.service;

import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.config.Config;
import com.gillsoft.entity.Resource;
import com.gillsoft.logging.SimpleRequestResponseLoggingInterceptor;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;
import com.gillsoft.util.RestTemplateUtil;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RestService {
	
	private static Logger LOGGER = LogManager.getLogger(RestService.class);
	
	private static final String LOCALITY_ALL = "locality/all";
	
	private static final String LOCALITY_USED = "locality/used";
	
	private static final String LOCALITY_BINDING = "locality/binding";
	
	private RestTemplate agregatorTemplate;
	
	private RestTemplate msTemplate;
	
	@Autowired
	@Qualifier("msAuthHeader")
	private HttpHeaders msAuth;
	
	public RestService() {
		agregatorTemplate = createNewPoolingTemplate(Config.getResourceAgregatorUrl());
		msTemplate = createNewPoolingTemplate(Config.getMsUrl());
	}
	
	public RestTemplate createNewPoolingTemplate(String host) {
		RestTemplate template = new RestTemplate(new BufferingClientHttpRequestFactory(
				RestTemplateUtil.createPoolingFactory(host, 10, Config.getRequestTimeout(), true, true)));
		template.setInterceptors(Collections.singletonList(
				new SimpleRequestResponseLoggingInterceptor()));
		return template;
	}
	
	public List<LocalityResponse> getAllLocalities(List<LocalityRequest> request) {
		return getLocalitiesResult(request, LOCALITY_ALL);
	}

	public List<LocalityResponse> getUsedLocalities(List<LocalityRequest> request) {
		return getLocalitiesResult(request, LOCALITY_USED);
	}

	public List<LocalityResponse> getLocalitiesBinding(List<LocalityRequest> request) {
		return getLocalitiesResult(request, LOCALITY_BINDING);
	}
	
	private List<LocalityResponse> getLocalitiesResult(List<LocalityRequest> request, String method) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + method).build().toUri();
		RequestEntity<List<LocalityRequest>> requestEntity = new RequestEntity<List<LocalityRequest>>(request, HttpMethod.POST, uri);
		try {
			ResponseEntity<List<LocalityResponse>> response = agregatorTemplate.exchange(requestEntity,
					new ParameterizedTypeReference<List<LocalityResponse>>() { });
			if (checkResponse(response, Config.getResourceAgregatorUrl() + method)) {
				return response.getBody();
			}
		} catch (RestClientException e) {
			LOGGER.error("REST execute error. Method: " + Config.getResourceAgregatorUrl() + method, e);
		}
		return null;
	}
	
	public List<Resource> getResources() {
		URI uri = UriComponentsBuilder.fromUriString(Config.getMsUrl()).build().toUri();
		RequestEntity<Object> requestEntity = new RequestEntity<Object>(msAuth, HttpMethod.GET, uri);
		try {
			ResponseEntity<List<Resource>> response = msTemplate.exchange(requestEntity,
					new ParameterizedTypeReference<List<Resource>>() { });
			if (checkResponse(response, Config.getMsUrl())) {
				return response.getBody();
			}
		} catch (RestClientException e) {
			LOGGER.error("REST execute error. Method: " + Config.getMsUrl(), e);
		}
		return null;
	}
	
	@Bean(name = "msAuthHeader")
	public HttpHeaders createMsAuthHeaders() {
		HttpHeaders headers = new HttpHeaders();
		String auth = Config.getMsLogin() + ":" + Config.getMsPassword();
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
		String authHeader = "Basic " + new String(encodedAuth);
		headers.add("Authorization", authHeader);
		return headers;
	}
	
	private <T> T getResult(RequestEntity<?> request, ParameterizedTypeReference<T> respType) throws ResponseError {
		try {
			ResponseEntity<T> response = msTemplate.exchange(request, respType);
			if (response.getStatusCode() != HttpStatus.ACCEPTED
					&& response.getStatusCode() != HttpStatus.OK) {
				throw new ResponseError("Response error: " + response.getStatusCode()
						+ " Method: " + request.getUrl().getPath());
			} else {
				return response.getBody();
			}
		} catch (RestClientException e) {
			LOGGER.error("REST execute error. Method: " + Config.getMsUrl(), e);
			throw new ResponseError("REST execute error. Method: " + Config.getMsUrl(), e);
		}
	}
	
	private boolean checkResponse(ResponseEntity<?> response, String method) {
		if (response.getStatusCode() != HttpStatus.ACCEPTED
				&& response.getStatusCode() != HttpStatus.OK) {
			LOGGER.error("Response error: " + response.getStatusCode()
					+ " Method: " + method);
			return false;
		}
		return true;
	}

}
