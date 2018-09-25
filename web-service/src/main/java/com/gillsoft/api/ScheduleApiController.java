package com.gillsoft.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.core.ScheduleController;
import com.gillsoft.model.request.ScheduleRequest;
import com.gillsoft.model.response.ScheduleResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/schedule")
@Api(tags = { "Schedule" }, produces = "application/json", consumes = "application/json")
public class ScheduleApiController {
	
	@Autowired
	private ScheduleController controller;
	
	@ApiOperation(value = "The schedule of selected resources",
			response = ScheduleResponse.class, responseContainer = "List")
	@PostMapping
	public List<ScheduleResponse> getSchedule(@Validated @RequestBody List<ScheduleRequest> request) {
		return controller.getSchedule(request);
	}

}
