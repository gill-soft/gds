package com.gillsoft.core.store;

import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.model.service.ResourceService;

public interface ResourceStore {
	
	public ResourceService getResourceService(ResourceParams params);

}
