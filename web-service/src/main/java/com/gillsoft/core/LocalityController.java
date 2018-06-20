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
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocalityController {
	
	@Autowired
	private ResourceStore store;
	
	@Autowired
	private ResourceActivity activity;
	
	public List<LocalityResponse> getAll(List<LocalityRequest> requests) {
		List<Callable<LocalityResponse>> callables = new ArrayList<>();
		for (final LocalityRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new LocalityResponse(request.getId(),
							store.getResourceService(request.getParams()).getLocalityService().getAll(request));
				} catch (Exception e) {
					return new LocalityResponse(request.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.LOCALITY, callables);
	}
	
	public List<LocalityResponse> getUsed(List<LocalityRequest> requests) {
		List<Callable<LocalityResponse>> callables = new ArrayList<>();
		for (final LocalityRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new LocalityResponse(request.getId(),
							store.getResourceService(request.getParams()).getLocalityService().getUsed(request));
				} catch (Exception e) {
					return new LocalityResponse(request.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.LOCALITY, callables);
	}
	
	public List<LocalityResponse> getBinding(List<LocalityRequest> requests) {
		List<Callable<LocalityResponse>> callables = new ArrayList<>();
		for (final LocalityRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new LocalityResponse(request.getId(),
							store.getResourceService(request.getParams()).getLocalityService().getBinding(request));
				} catch (Exception e) {
					return new LocalityResponse(request.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.LOCALITY, callables);
	}

}
