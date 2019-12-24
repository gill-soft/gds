package com.gillsoft.control.core;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.ContextProvider;

public class MethodsUpdateTask extends MsDataObjectUpdateTask implements Serializable {
	
	private static final long serialVersionUID = 3342891694092388685L;
	
	private Resource resource;
	private String userName;

	public MethodsUpdateTask(Resource resource, String userName) {
		this.resource = resource;
		this.userName = userName;
	}

	@Override
	protected String getCacheKey() {
		return ResourceInfoController.getActiveResourcesCacheKey(resource.getId(), userName);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		ResourceInfoController controller = ContextProvider.getBean(ResourceInfoController.class);
		return controller.createMethods(resource, userName);
	}

}
