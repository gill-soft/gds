package com.gillsoft.model.service;

import com.gillsoft.model.request.ScheduleRequest;
import com.gillsoft.model.response.ScheduleResponse;

public interface ScheduleService {
	
	public ScheduleResponse getSchedule(ScheduleRequest request);

}
