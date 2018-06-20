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
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.ResourceMethodResponse;
import com.gillsoft.model.response.ResourceResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResourceController {
	
	@Autowired
	private ResourceStore store;
	
	@Autowired
	private ResourceActivity activity;

	public List<ResourceResponse> getResources(List<ResourceRequest> requests) {
		List<Callable<ResourceResponse>> callables = new ArrayList<>();
		for (final ResourceRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new ResourceResponse(request.getId(),
							store.getResourceService(request.getParams()).getInfo());
				} catch (Exception e) {
					return new ResourceResponse(request.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.RESOURCE_INFO, callables);
	}
	
	public List<ResourceMethodResponse> getMethods(List<ResourceRequest> requests) {
		List<Callable<ResourceMethodResponse>> callables = new ArrayList<>();
		for (final ResourceRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return new ResourceMethodResponse(request.getId(),
							store.getResourceService(request.getParams()).getAvailableMethods());
				} catch (Exception e) {
					return new ResourceMethodResponse(request.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.RESOURCE_INFO, callables);
	}
	
}
