package com.gillsoft.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;
import com.gillsoft.service.AgregatorLocalityService;

@Service
public class AgregatorLocalityRestService extends AbstractAgregatorRestService implements AgregatorLocalityService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorLocalityRestService.class);

	@Override
	public List<LocalityResponse> getAll(List<LocalityRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LocalityResponse> getUsed(List<LocalityRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LocalityResponse> getBinding(List<LocalityRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
