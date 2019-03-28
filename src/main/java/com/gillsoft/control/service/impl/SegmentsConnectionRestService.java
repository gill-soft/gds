package com.gillsoft.control.service.impl;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.SegmentsConnectionService;
import com.gillsoft.control.service.model.ConnectionParams;
import com.gillsoft.control.service.model.ConnectionsResponse;
import com.gillsoft.model.ResponseError;

@Service
public class SegmentsConnectionRestService extends AbstractRestService implements SegmentsConnectionService {
	
	private static Logger LOGGER = LogManager.getLogger(SegmentsConnectionRestService.class);
	
	private static final String CREATE = "/connection/create";
	
	private RestTemplate template;

	@Override
	public ConnectionsResponse getConnections(ConnectionParams params) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Config.getSegmentsConnectionUrl() + CREATE);
		URI uri = builder.build().toUri();
		RequestEntity<ConnectionParams> entity = new RequestEntity<ConnectionParams>(params, HttpMethod.POST, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<ConnectionsResponse>() {});
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
			synchronized (SegmentsConnectionRestService.class) {
				if (template == null) {
					template = createTemplate(Config.getMsUrl());
				}
			}
		}
		return template;
	}

}
