package com.gillsoft.core.store;

import com.gillsoft.core.service.ResourceService;
import com.gillsoft.model.request.ResourceParams;

public interface ResourceStore {
	
	public ResourceService getResourceService(ResourceParams params);

}
