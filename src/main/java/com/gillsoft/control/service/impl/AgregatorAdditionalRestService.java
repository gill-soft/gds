package com.gillsoft.control.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gillsoft.control.service.AgregatorAdditionalService;

@Service
public class AgregatorAdditionalRestService extends AbstractAgregatorRestService implements AgregatorAdditionalService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorAdditionalRestService.class);

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
