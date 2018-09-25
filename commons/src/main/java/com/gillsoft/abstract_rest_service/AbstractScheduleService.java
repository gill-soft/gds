package com.gillsoft.abstract_rest_service;

import org.springframework.web.bind.annotation.PostMapping;

import com.gillsoft.model.Method;
import com.gillsoft.model.request.ScheduleRequest;
import com.gillsoft.model.response.ScheduleResponse;
import com.gillsoft.model.service.ScheduleService;

public abstract class AbstractScheduleService implements ScheduleService {

	@Override
	@PostMapping(Method.SCHEDULE)
	public final ScheduleResponse getSchedule(ScheduleRequest request) {
		return getScheduleResponse(request);
	}
	
	public abstract ScheduleResponse getScheduleResponse(ScheduleRequest request);

}
