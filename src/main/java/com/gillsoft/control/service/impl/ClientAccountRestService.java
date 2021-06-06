package com.gillsoft.control.service.impl;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.ClientAccountService;
import com.gillsoft.control.service.model.ClientView;
import com.gillsoft.model.ResponseError;
import com.gillsoft.ms.entity.Client;

@Service
public class ClientAccountRestService extends AbstractRestService implements ClientAccountService {
	
	private static Logger LOGGER = LogManager.getLogger(ClientAccountRestService.class);
	
	private static final String REGISTER = "account/register";
	
	private static final String BY_USER = "account/by_user";
	
	private RestTemplate template;
	
	@Autowired
	@Qualifier("msAuthHeader")
	private HttpHeaders msAuth;

	@Override
	public ClientView register(ClientView client) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getClientAccountUrl() + REGISTER).build().toUri();
		RequestEntity<Client> entity = new RequestEntity<Client>(client, msAuth, HttpMethod.POST, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<ClientView>() { });
		} catch (ResponseError e) {
			return null;
		}
	}
	
	@Override
	public ClientView getByUser(String clientName) {
		URI uri = UriComponentsBuilder.fromUriString(Config.getClientAccountUrl() + BY_USER)
				.queryParam("clientName", clientName).build().toUri();
		RequestEntity<Client> entity = new RequestEntity<Client>(msAuth, HttpMethod.GET, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<ClientView>() { });
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
			synchronized (MsDataRestService.class) {
				if (template == null) {
					template = createTemplate(Config.getMsUrl());
				}
			}
		}
		return template;
	}

}
