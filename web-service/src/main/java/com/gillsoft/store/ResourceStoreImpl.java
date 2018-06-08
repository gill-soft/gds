package com.gillsoft.store;

import org.springframework.stereotype.Component;

import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.service.ResourceService;
import com.gillsoft.service.rest.RestResourceService;

@Component
public class ResourceStoreImpl implements ResourceStore {

	@Override
	public ResourceService getResourceService(ResourceParams params) {
		
		/*
		 * по одному из параметров должен определяться тип сервиса ресурса,
		 * пока только один тип REST
		 */
		ResourceService service = new RestResourceService();
		service.applayParams(params);
		return service;
	}

}
