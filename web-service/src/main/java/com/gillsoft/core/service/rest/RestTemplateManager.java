package com.gillsoft.core.service.rest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.web.client.RestTemplate;

import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.util.RestTemplateUtil;

public class RestTemplateManager {
	
	private static ConcurrentMap<ResourceParams, RestTemplate> templates = new ConcurrentHashMap<>();
	
	public static RestTemplate getTemplate(ResourceParams params) {
		
		/*
		 * чтобы не плодить на одни и те же параметры новые RestTemplate -
		 * заполняем мапу и берем их оттуда
		 */
		RestTemplate template = templates.get(params);
		if (template == null) {
			template = createNewPoolingTemplate(params);
			templates.put(params, template);
		}
		return template;
	}
	
	public static RestTemplate createNewPoolingTemplate(ResourceParams params) {
		
		// создаем пул соединений
		return new RestTemplate(
				RestTemplateUtil.createPoolingFactory(params.getHost(), 50,
						params.getRequestTimeout() != null ? params.getRequestTimeout() : 5000));
	}

}
