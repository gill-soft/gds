package com.gillsoft.control.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gillsoft.control.service.AgregatorAdditionalService;
import com.gillsoft.control.service.AgregatorLocalityService;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.control.service.AgregatorResourceInfoService;
import com.gillsoft.control.service.AgregatorScheduleService;
import com.gillsoft.control.service.AgregatorService;
import com.gillsoft.control.service.AgregatorTripSearchService;

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
