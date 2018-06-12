package com.gillsoft.core.service.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.core.service.AdditionalService;
import com.gillsoft.core.service.LocalityService;
import com.gillsoft.core.service.OrderService;
import com.gillsoft.core.service.ResourceService;
import com.gillsoft.core.service.SearchService;
import com.gillsoft.core.service.TicketService;
import com.gillsoft.model.Method;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceParams;

public class RestResourceService implements ResourceService {
	
	private RestTemplate template;
	private ResourceParams params;
	
	private Map<String, ?> getMap() {
		return new HashMap<>(0);
	}
	
	@Override
	public void applayParams(ResourceParams params) {
		this.params = params;
		template = RestTemplateManager.getTemplate(params);
	}
	
	private String getHost() {
		StringBuilder host = new StringBuilder();
		host.append(params.getHost());
		return host.toString();
	}

	@Override
	public boolean isAvailable() {
		String uuid = UUID.randomUUID().toString();
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(getHost() + Method.PING)
				.queryParam("id", uuid);
		ResponseEntity<Ping> resp = template.getForEntity(builder.toUriString(), Ping.class, getMap());
		return resp.getStatusCode() == HttpStatus.OK
				&& Objects.equals(resp.getBody().getId(), uuid);
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
	public LocalityService getLocationService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchService getSearchService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TicketService getTicketService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderService getOrderService() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AdditionalService getAdditionalService() {
		// TODO Auto-generated method stub
		return null;
	}

}
