package com.gillsoft.util;

import java.util.Collections;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

public abstract class RestTemplateUtil {
	
 	public static ClientHttpRequestFactory createPoolingFactory(String url, int maxConnections, int requestTimeout) {
		
		// создаем пул соединений
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(url)), maxConnections);
		
		HttpClient httpClient = HttpClients.custom()
		        .setConnectionManager(connectionManager)
		        .build();
		
		// настраиваем таймауты
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(10000);
		factory.setConnectionRequestTimeout(requestTimeout);
		factory.setHttpClient(httpClient);
		return factory;
 	}
	
	public static List<HttpMessageConverter<?>> getMarshallingMessageConverters(Class<?>... classesToBeBound) {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(classesToBeBound);
		return Collections.singletonList(new MarshallingHttpMessageConverter(marshaller, marshaller));
	}

}
