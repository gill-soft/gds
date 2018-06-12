package com.gillsoft.core;

import java.rmi.AccessException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.core.store.ResourceStore;
import com.gillsoft.model.Method;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceRequest;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResourceController {
	
	@Autowired
	private ResourceStore store;
	
	@Autowired
	private ResourceActivity activity;

	public List<Resource> getResources(List<ResourceRequest> requests) {
		List<Callable<Resource>> callables = new ArrayList<>();
		for (final ResourceRequest request : requests) {
			callables.add(() -> {
				try {
					activity.check(request);
					return store.getResourceService(request.getParams()).getInfo();
				} catch (AccessException e) {
					return new Resource(e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.RESOURCE_INFO, callables);
	}
	
	public List<Method> getMethods(ResourceRequest request) throws AccessException {
		activity.check(request);
		return store.getResourceService(request.getParams()).getAvailableMethods();
	}
	
}
