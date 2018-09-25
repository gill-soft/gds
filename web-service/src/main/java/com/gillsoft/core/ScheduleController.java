package com.gillsoft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.core.store.ResourceStore;
import com.gillsoft.model.request.ScheduleRequest;
import com.gillsoft.model.response.ScheduleResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ScheduleController {
	
	@Autowired
	private ResourceStore store;
	
	@Autowired
	private ResourceActivity activity;
	
	public List<ScheduleResponse> getSchedule(List<ScheduleRequest> requests) {
		List<Callable<ScheduleResponse>> callables = new ArrayList<>();
		for (final ScheduleRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					ScheduleResponse response = store.getResourceService(request.getParams()).getScheduleService().getSchedule(request);
					response.setId(request.getId());
					return response;
				} catch (Exception e) {
					return new ScheduleResponse(request.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.LOCALITY, callables);
	}

}
