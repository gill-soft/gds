package com.gillsoft.service.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.gillsoft.model.request.ScheduleRequest;
import com.gillsoft.model.response.ScheduleResponse;
import com.gillsoft.service.AgregatorScheduleService;

@Service
public class AgregatorScheduleRestService extends AbstractAgregatorRestService implements AgregatorScheduleService {
	
	private static Logger LOGGER = LogManager.getLogger(AgregatorScheduleRestService.class);

	@Override
	public List<ScheduleResponse> getSchedule(List<ScheduleRequest> request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

}
