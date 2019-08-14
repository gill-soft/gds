package com.gillsoft.control.service;

import com.gillsoft.model.response.TripSearchResponse;

public interface ScheduleService {
	
	public TripSearchResponse getSegmentResponse(long resourceId, String segmentId);

}
