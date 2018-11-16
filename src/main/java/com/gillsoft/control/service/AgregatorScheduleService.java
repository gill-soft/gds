package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.model.request.ScheduleRequest;
import com.gillsoft.model.response.ScheduleResponse;

public interface AgregatorScheduleService {
	
	public List<ScheduleResponse> getSchedule(List<ScheduleRequest> request);

}
