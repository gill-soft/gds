package com.gillsoft.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gillsoft.service.AgregatorAdditionalService;
import com.gillsoft.service.AgregatorLocalityService;
import com.gillsoft.service.AgregatorOrderService;
import com.gillsoft.service.AgregatorResourceInfoService;
import com.gillsoft.service.AgregatorScheduleService;
import com.gillsoft.service.AgregatorService;
import com.gillsoft.service.AgregatorTripSearchService;

@Service
public class AgregatorRestService implements AgregatorService {
	
	@Autowired
	private AgregatorResourceInfoService resourceInfoService;
	
	@Autowired
	private AgregatorLocalityService localityService;
	
	@Autowired
	private AgregatorTripSearchService searchSearvice;
	
	@Autowired
	private AgregatorOrderRestService orderService;
	
	@Autowired
	private AgregatorAdditionalService additionalService;
	
	@Autowired
	private AgregatorScheduleService scheduleService;

	@Override
	public AgregatorResourceInfoService getResourceInfoService() {
		return resourceInfoService;
	}

	@Override
	public AgregatorLocalityService getLocalityService() {
		return localityService;
	}

	@Override
	public AgregatorTripSearchService getSearchService() {
		return searchSearvice;
	}

	@Override
	public AgregatorOrderService getOrderService() {
		return orderService;
	}

	@Override
	public AgregatorAdditionalService getAdditionalService() {
		return additionalService;
	}

	@Override
	public AgregatorScheduleService getScheduleService() {
		return scheduleService;
	}

}
