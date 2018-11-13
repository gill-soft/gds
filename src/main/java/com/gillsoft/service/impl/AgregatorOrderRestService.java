package com.gillsoft.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.service.AgregatorOrderService;

@Service
public class AgregatorOrderRestService extends AbstractAgregatorRestService implements AgregatorOrderService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorOrderRestService.class);

	@Override
	public OrderResponse create(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> addServices(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> removeServices(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> updateCustomers(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> get(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> getService(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> booking(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> confirm(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> cancel(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> prepareReturnServices(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> returnServices(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<OrderResponse> getPdfDocuments(List<OrderRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
