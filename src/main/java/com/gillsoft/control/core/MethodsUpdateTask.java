package com.gillsoft.control.core;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.ContextProvider;

public class MethodsUpdateTask extends MsDataObjectUpdateTask implements Serializable {
	
	private static final long serialVersionUID = -5904967749931931823L;
	
	private Resource resource;

	public MethodsUpdateTask(Resource resource) {
		this.resource = resource;
	}

	@Override
	protected String getCacheKey() {
		return ResourceInfoController.getActiveMethodsCacheKey(resource.getId());
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		ResourceInfoController controller = ContextProvider.getBean(ResourceInfoController.class);
		return controller.createMethods(resource);
	}

}
