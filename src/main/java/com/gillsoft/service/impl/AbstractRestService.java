package com.gillsoft.service.impl;

import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.gillsoft.config.Config;
import com.gillsoft.logging.SimpleRequestResponseLoggingInterceptor;
import com.gillsoft.model.ResponseError;
import com.gillsoft.util.RestTemplateUtil;

public abstract class AbstractRestService {
	
	public abstract RestTemplate getTemplate();
	
	public RestTemplate createTemplate(String url) {
		RestTemplate template = new RestTemplate(new BufferingClientHttpRequestFactory(
				RestTemplateUtil.createPoolingFactory(url, 100, Config.getRequestTimeout(), true, true)));
		template.setInterceptors(Collections.singletonList(
				new SimpleRequestResponseLoggingInterceptor()));
		return template;
	}
	
	protected <T> T getResult(RequestEntity<?> request, ParameterizedTypeReference<T> respType) throws ResponseError {
		try {
			ResponseEntity<T> response = getTemplate().exchange(request, respType);
			if (response.getStatusCode() != HttpStatus.ACCEPTED
					&& response.getStatusCode() != HttpStatus.OK) {
				throw new ResponseError("Response error: " + response.getStatusCode()
						+ " Method: " + request.getUrl().getPath());
			} else {
				return response.getBody();
			}
		} catch (RestClientException e) {
			getLogger().error("REST execute error. Method: " + Config.getMsUrl(), e);
			throw new ResponseError("REST execute error. Method: " + Config.getMsUrl(), e);
		}
	}
	
	protected abstract Logger getLogger();
	
}
