package com.gillsoft.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Marshaller;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestClientException;

public abstract class RestTemplateUtil {
	
 	public static ClientHttpRequestFactory createPoolingFactory(String url, int maxConnections, int requestTimeout) {
		
		return createPoolingFactory(url, maxConnections, requestTimeout, false, false);
 	}
 	
	public static ClientHttpRequestFactory createPoolingFactory(String url, int maxConnections, int requestTimeout,
			boolean disableAuthCaching, boolean disableCookieManagement) {

		// создаем пул соединений
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(url)), maxConnections);
		
		HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
		if (disableAuthCaching) {
			httpClientBuilder.disableAuthCaching();
		}
		if (disableCookieManagement) {
			httpClientBuilder.disableCookieManagement();
		}
		HttpClient httpClient = httpClientBuilder.build();
		
		// настраиваем таймауты
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setConnectTimeout(1000);
		factory.setConnectionRequestTimeout(requestTimeout);
		factory.setHttpClient(httpClient);
		return factory;
 	}
	
	public static List<HttpMessageConverter<?>> getMarshallingMessageConverters(Class<?>... classesToBeBound) {
		return getMarshallingMessageConverters(null, classesToBeBound);
	}
	
	public static List<HttpMessageConverter<?>> getMarshallingMessageConverters(String encoding, Class<?>... classesToBeBound) {
		if (encoding != null) {
			Map<String, String> properties = new HashMap<>();
			properties.put(Marshaller.JAXB_ENCODING, encoding);
			return getMarshallingMessageConverters(properties, properties, classesToBeBound);
		}
		return getMarshallingMessageConverters(null, null, classesToBeBound);
	}
	
	public static List<HttpMessageConverter<?>> getMarshallingMessageConverters(
			Map<String, String> marshProp, Map<String, String> unmarshProp, Class<?>... classesToBeBound) {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		marshaller.setClassesToBeBound(classesToBeBound);
		if (marshProp != null) {
			marshaller.setMarshallerProperties(marshProp);
		}
		if (unmarshProp != null) {
			marshaller.setUnmarshallerProperties(unmarshProp);
		}
		return Collections.singletonList(new MarshallingHttpMessageConverter(marshaller, marshaller));
	}
	
	public static RestClientException createUnavailableMethod() {
		return new RestClientException("Method is unavailable");
	}
	
	public static RestClientException createRestException(String message) {
		return new RestClientException(message);
	}
	
	public static RestClientException createRestException(String message, Throwable exception) {
		return new RestClientException(message, exception);
	}

}
