package com.gillsoft.control.service.impl;

import org.springframework.web.client.RestTemplate;

import com.gillsoft.control.config.Config;

public abstract class AbstractAgregatorRestService extends AbstractRestService {
	
	private static Object synch = new Object();
	
	private static RestTemplate template;

	public RestTemplate getTemplate() {
		if (template == null) {
			synchronized (synch) {
				if (template == null) {
					template = createTemplate(Config.getResourceAgregatorUrl());
				}
			}
		}
		return template;
	}

}
