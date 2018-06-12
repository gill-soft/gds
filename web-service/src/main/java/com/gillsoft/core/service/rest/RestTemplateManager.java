package com.gillsoft.core.service.rest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.gillsoft.model.request.ResourceParams;

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
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxPerRoute(new HttpRoute(getHttpHost(params)), 50);
		
		HttpClient httpClient = HttpClients.custom()
		        .setConnectionManager(connectionManager)
		        .build();
		
		// настраиваем таймауты
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(1000);
		factory.setConnectionRequestTimeout(params.getRequestTimeout() > 0 ? params.getRequestTimeout() : 500);
		factory.setHttpClient(httpClient);
		
		RestTemplate template = new RestTemplate(factory);
		return template;
	}
	
	private static HttpHost getHttpHost(ResourceParams params) {
		return new HttpHost(params.getHost());
	}

}
