package com.gillsoft.control.service.impl;

import java.net.URI;
import java.text.MessageFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.ScheduleService;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.response.TripSearchResponse;

@Service
public class ScheduleRestService extends AbstractRestService implements ScheduleService {

	private static Logger LOGGER = LogManager.getLogger(ScheduleRestService.class);

	private static final String GET_SEGMENT_RESPONSE = "/data/search/{0}/{1}";

	private RestTemplate template;

	@Override
	public TripSearchResponse getSegmentResponse(long resourceId, String segmentId) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Config.getScheduleUrl()
				+ MessageFormat.format(GET_SEGMENT_RESPONSE, String.valueOf(resourceId), segmentId));
		URI uri = builder.build().toUri();
		RequestEntity<Object> entity = new RequestEntity<Object>(HttpMethod.GET, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<TripSearchResponse>() { });
		} catch (ResponseError e) {
			return null;
		}
	}

	@Override
	public RestTemplate getTemplate() {
		if (template == null) {
			synchronized (ScheduleRestService.class) {
				if (template == null) {
					template = createTemplate(Config.getScheduleUrl());
				}
			}
		}
		return template;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
