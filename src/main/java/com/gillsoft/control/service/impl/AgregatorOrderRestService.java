package com.gillsoft.control.service.impl;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.gillsoft.control.config.Config;
import com.gillsoft.control.core.Utils;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

@Service
public class AgregatorOrderRestService extends AbstractAgregatorRestService implements AgregatorOrderService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorOrderRestService.class);
	
	private static final String ORDER = "order";
	
	private static final String CONFIRM = "order/confirm";
	
	private static final String BOOKING = "order/booking";
	
	private static final String RETURN_PREPARE = "order/return/prepare";
	
	private static final String RETURN_CONFIRM = "order/return/confirm";
	
	private static final String CANCEL = "order/cancel";
	
	private static final String INFO = "order/info";
	
	private static final String SERVICE = "order/service";
	
	private static final String DOCUMENT = "order/document";

	@Override
	public OrderResponse create(OrderRequest request) {
		try {
			request = (OrderRequest) SerializationUtils.deserialize(SerializationUtils.serialize(request));
			request.getResources().removeIf(r -> !Utils.isPresentHost(r));
			URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + ORDER).build().toUri();
			RequestEntity<OrderRequest> entity = new RequestEntity<OrderRequest>(request, HttpMethod.POST, uri);
			return getResult(entity, new ParameterizedTypeReference<OrderResponse>() { });
		} catch (ResponseError e) {
			return new OrderResponse(null, e);
		}
	}

	@Override
	public List<OrderResponse> addServices(List<OrderRequest> requests) {
		//TODO
		return null;
	}

	@Override
	public List<OrderResponse> removeServices(List<OrderRequest> requests) {
		//TODO
		return null;
	}

	@Override
	public List<OrderResponse> updateCustomers(List<OrderRequest> requests) {
		//TODO
		return null;
	}

	@Override
	public List<OrderResponse> get(List<OrderRequest> requests) {
		return getResult(requests, INFO);
	}

	@Override
	public List<OrderResponse> getService(List<OrderRequest> requests) {
		return getResult(requests, SERVICE);
	}

	@Override
	public List<OrderResponse> booking(List<OrderRequest> requests) {
		return getResult(requests, BOOKING);
	}

	@Override
	public List<OrderResponse> confirm(List<OrderRequest> requests) {
		return getResult(requests, CONFIRM);
	}

	@Override
	public List<OrderResponse> cancel(List<OrderRequest> requests) {
		return getResult(requests, CANCEL);
	}

	@Override
	public List<OrderResponse> prepareReturnServices(List<OrderRequest> requests) {
		return getResult(requests, RETURN_PREPARE);
	}

	@Override
	public List<OrderResponse> returnServices(List<OrderRequest> requests) {
		return getResult(requests, RETURN_CONFIRM);
	}

	@Override
	public List<OrderResponse> getPdfDocuments(List<OrderRequest> requests) {
		return getResult(requests, DOCUMENT);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}
	
	private List<OrderResponse> getResult(List<OrderRequest> request, String method) {
		if (request.stream().noneMatch(r -> Utils.isPresentHost(r))) {
			return null;
		}
		request = request.stream().filter(r -> Utils.isPresentHost(r)).collect(Collectors.toList());
		URI uri = UriComponentsBuilder.fromUriString(Config.getResourceAgregatorUrl() + method).build().toUri();
		RequestEntity<List<OrderRequest>> entity = new RequestEntity<List<OrderRequest>>(request, HttpMethod.POST, uri);
		try {
			return getResult(entity, new ParameterizedTypeReference<List<OrderResponse>>() { });
		} catch (ResponseError e) {
			return Collections.singletonList(new OrderResponse(null, e));
		}
	}

}
