package com.gillsoft.core.store;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.core.service.rest.RestResourceService;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.model.service.ResourceService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
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
