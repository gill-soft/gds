package com.gillsoft.control.service.impl;

import java.net.URI;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.PrintTicketService;
import com.gillsoft.control.service.model.PrintOrderWrapper;
import com.gillsoft.model.Document;
import com.gillsoft.model.ResponseError;

@Service
public class PrintTicketRestService extends AbstractRestService implements PrintTicketService {
	
	private static Logger LOGGER = LogManager.getLogger(PrintTicketRestService.class);
	
	private static final String PRINT_METHOD = "order/print";
	
	private RestTemplate template;

	@Override
	public List<Document> create(PrintOrderWrapper orderWrapper) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Config.getMsUrl() + PRINT_METHOD);
		URI uri = builder.build().toUri();
		RequestEntity<PrintOrderWrapper> entity = new RequestEntity<PrintOrderWrapper>(orderWrapper, HttpMethod.POST, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<List<Document>>() { });
		} catch (ResponseError e) {
			return null;
		}
	}

	@Override
	public RestTemplate getTemplate() {
		if (template == null) {
			synchronized (PrintTicketRestService.class) {
				if (template == null) {
					template = createTemplate(Config.getPrintTicketUrl());
				}
			}
		}
		return template;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
