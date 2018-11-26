package com.gillsoft.control.service.impl;

import java.net.URI;
import java.util.Base64;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.model.ResponseError;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.User;

@Service
public class MsDataRestService extends AbstractRestService implements MsDataService {
	
	private static Logger LOGGER = LogManager.getLogger(MsDataRestService.class);
	
	private static final Object synch = new Object();
	
	private static final String ALL_COMMISSIONS = "commission/all_with_parents";
	
	private static final String GET_USER = "user/by_name_with_parents/{name}";
	
	private static final String GET_USER_ORGANISATION = "user/{name}/organisation";
	
	private static final String GET_USER_RESOURCES = "user/{name}/resources";
	
	private RestTemplate template;
	
	@Autowired
	@Qualifier("msAuthHeader")
	private HttpHeaders msAuth;
	
	@Bean(name = "msAuthHeader")
	public HttpHeaders createMsAuthHeaders() {
		HttpHeaders headers = new HttpHeaders();
		String auth = Config.getMsLogin() + ":" + Config.getMsPassword();
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
		String authHeader = "Basic " + new String(encodedAuth);
		headers.add("Authorization", authHeader);
		return headers;
	}

	@Override
	public List<Resource> getUserResources(String userName) {
		return getResultByUser(userName, GET_USER_RESOURCES, new ParameterizedTypeReference<List<Resource>>() { });
	}

	@Override
	public User getUser(String userName) {
		return getResultByUser(userName, GET_USER, new ParameterizedTypeReference<User>() { });
	}
	
	@Override
	public Organisation getUserOrganisation(String userName) {
		return getResultByUser(userName, GET_USER_ORGANISATION, new ParameterizedTypeReference<Organisation>() { });
	}

	@Override
	public List<Commission> getAllCommissions() {
		return getResult(ALL_COMMISSIONS, null, new ParameterizedTypeReference<List<Commission>>() { });
	}
	
	private <T> T getResultByUser(String userName, String method, ParameterizedTypeReference<T> type) {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>(1);
		params.add("name", userName);
		return getResult(method, params, type);
	}
	
	private <T> T getResult(String method, MultiValueMap<String, String> params, ParameterizedTypeReference<T> type) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Config.getMsUrl() + method);
		if (params != null) {
			builder.queryParams(params);
		}
		URI uri = builder.build().toUri();
		RequestEntity<Object> entity = new RequestEntity<Object>(msAuth, HttpMethod.GET, uri);
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

	@Override
	public RestTemplate getTemplate() {
		if (template == null) {
			synchronized (synch) {
				if (template == null) {
					template = createTemplate(Config.getMsUrl());
				}
			}
		}
		return template;
	}

}
