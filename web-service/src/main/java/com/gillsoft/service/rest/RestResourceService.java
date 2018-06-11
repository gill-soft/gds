package com.gillsoft.service.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.gillsoft.model.Method;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.service.AdditionalService;
import com.gillsoft.service.LocalityService;
import com.gillsoft.service.OrderService;
import com.gillsoft.service.ResourceService;
import com.gillsoft.service.SearchService;
import com.gillsoft.service.TicketService;

public class RestResourceService implements ResourceService {
	
	private RestTemplate template;
	
	private Map<String, ?> getMap() {
		return null;
	}
	
	@Override
	public void applayParams(ResourceParams params) {
		template = RestTemplateManager.getTemplate(params);
	}

	@Override
	public boolean isAvailable() {
		ResponseEntity<String> resp = template.getForEntity(Method.PING, String.class, getMap());
		return resp.getStatusCode() == HttpStatus.OK;
	}

	@Override
	public Resource getInfo() {
		ResponseEntity<Resource> resp = template.getForEntity(Method.INFO, Resource.class, getMap());
		return resp.getBody();
	}

	@Override
	public List<Method> getAvailableMethods() {
		ResponseEntity<Method[]> resp = template.getForEntity(Method.INFO, Method[].class, getMap());
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
