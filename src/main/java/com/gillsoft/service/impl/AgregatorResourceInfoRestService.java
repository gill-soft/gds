package com.gillsoft.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.ResourceMethodResponse;
import com.gillsoft.model.response.ResourceResponse;
import com.gillsoft.service.AgregatorResourceInfoService;

@Service
public class AgregatorResourceInfoRestService extends AbstractAgregatorRestService
		implements AgregatorResourceInfoService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorResourceInfoRestService.class);

	@Override
	public List<ResourceResponse> getInfo(List<ResourceRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ResourceMethodResponse> getAvailableMethods(List<ResourceRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
