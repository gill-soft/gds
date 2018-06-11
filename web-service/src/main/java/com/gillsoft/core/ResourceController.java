package com.gillsoft.core;

import java.rmi.AccessException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.gillsoft.model.Method;
import com.gillsoft.model.Resource;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.store.ResourceStore;

public class ResourceController {
	
	@Autowired
	private ResourceStore store;
	
	private static ResourceController instance;
	
	public static ResourceController getInstance() {
		if (instance == null) {
			synchronized (ResourceController.class) {
				if (instance == null) {
					instance = new ResourceController();
				}
			}
		}
		return instance;
	}
	
	public static ResourceController newInstance(ResourceStore store) {
		ResourceController instance = new ResourceController();
		instance.setStore(store);
		return instance;
	}
	
	private void setStore(ResourceStore store) {
		this.store = store;
	}

	public List<Resource> getResources(List<ResourceRequest> requests) {
		List<Callable<Resource>> callables = new ArrayList<>();
		List<Resource> result = new ArrayList<>(requests.size());
		for (final ResourceRequest request : requests) {
			try {
				ResourceActivity.getInstance().check(request);
				callables.add(() -> {
					return store.getResourceService(request.getParams()).getInfo();
				});
			} catch (AccessException e) {
				result.add(new Resource(e));
			}
		}
		return ThreadPoolStore.getResult(PoolType.RESOURCE_INFO, callables);
	}
	
	public List<Method> getMethods(ResourceRequest request) throws AccessException {
		ResourceActivity.getInstance().check(request);
		return store.getResourceService(request.getParams()).getAvailableMethods();
	}
	
}
