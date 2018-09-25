package com.gillsoft.core.service.rest;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.model.Method;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.model.service.AdditionalService;
import com.gillsoft.model.service.LocalityService;
import com.gillsoft.model.service.OrderService;
import com.gillsoft.model.service.ResourceService;
import com.gillsoft.model.service.ScheduleService;
import com.gillsoft.model.service.TripSearchService;
import com.gillsoft.util.StringUtil;

// TODO добавить обработку статусов ответов ResponseEntity
public class RestResourceService implements ResourceService {
	
	private RestTemplate template;
	private ResourceParams params;
	private RestLocalityService localityService;
	private RestTripSearchService searchService;
	private RestOrderService orderService;
	private RestScheduleService scheduleService;
	
	private Map<String, ?> getMap() {
		return new HashMap<>(0);
	}
	
	@Override
	public void applayParams(ResourceParams params) {
		this.params = params;
		template = RestTemplateManager.getTemplate(params);
	}
	
	public RestTemplate getTemplate() {
		return template;
	}

	public String getHost() {
		StringBuilder host = new StringBuilder();
		host.append(params.getHost());
		return host.toString();
	}

	public boolean isAvailable() {
		String uuid = StringUtil.generateUUID();
		try {
			return Objects.equals(ping(uuid).getId(), uuid);
		} catch (RestClientException e) {
			return false;
		}
	}
	
	@Override
	public Ping ping(String id) {
		URI uri = UriComponentsBuilder.fromUriString(getHost() + Method.PING)
				.queryParam("id", id).build().toUri();
		return template.getForEntity(uri, Ping.class).getBody();
	}

	@Override
	public Resource getInfo() {
		ResponseEntity<Resource> resp = template.getForEntity(getHost() + Method.INFO, Resource.class, getMap());
		return resp.getBody();
	}

	@Override
	public List<Method> getAvailableMethods() {
		ResponseEntity<Method[]> resp = template.getForEntity(getHost() + Method.METHOD, Method[].class, getMap());
		return Arrays.asList(resp.getBody());
	}

	@Override
	public LocalityService getLocalityService() {
		if (localityService == null) {
			localityService = new RestLocalityService();
			localityService.setResourceService(this);
		}
		return localityService;
	}

	@Override
	public TripSearchService getSearchService() {
		if (searchService == null) {
			searchService = new RestTripSearchService();
			searchService.setResourceService(this);
		}
		return searchService;
	}

	@Override
	public OrderService getOrderService() {
		if (orderService == null) {
			orderService = new RestOrderService();
			orderService.setResourceService(this);
		}
		return orderService;
	}

	@Override
	public AdditionalService getAdditionalService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ScheduleService getScheduleService() {
		if (scheduleService == null) {
			scheduleService = new RestScheduleService();
			scheduleService.setResourceService(this);
		}
		return scheduleService;
	}

}
