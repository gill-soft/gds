package com.gillsoft.core.service.rest;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.model.Method;
import com.gillsoft.model.request.ScheduleRequest;
import com.gillsoft.model.response.ScheduleResponse;
import com.gillsoft.model.service.ScheduleService;

public class RestScheduleService implements ScheduleService {
	
	private RestResourceService resourceService;

	@Override
	public ScheduleResponse getSchedule(ScheduleRequest request) {
		URI uri = UriComponentsBuilder.fromUriString(resourceService.getHost() + Method.SCHEDULE).build().toUri();
		ResponseEntity<ScheduleResponse> response = resourceService.getTemplate().postForEntity(uri, request, ScheduleResponse.class);
		return response.getBody();
	}

	public void setResourceService(RestResourceService resourceService) {
		this.resourceService = resourceService;
	}

}
