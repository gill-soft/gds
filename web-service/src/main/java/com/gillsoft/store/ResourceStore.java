package com.gillsoft.store;

import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.service.ResourceService;

public interface ResourceStore {
	
	public ResourceService getResourceService(ResourceParams params);

}
