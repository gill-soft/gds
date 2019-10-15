package com.gillsoft.control.core;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.util.ContextProvider;

public class MethodsUpdateTask extends MsDataObjectUpdateTask implements Serializable {
	
	private static final long serialVersionUID = 1879964997804865915L;
	
	private ResourceRequest request;

	public MethodsUpdateTask(ResourceRequest request) {
		this.request = request;
	}

	@Override
	protected String getCacheKey() {
		return ResourceInfoController.getActiveResourcesCacheKey(Long.parseLong(request.getParams().getResource().getId()));
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		ResourceInfoController controller = ContextProvider.getBean(ResourceInfoController.class);
		return controller.createMethods(request);
	}

}
