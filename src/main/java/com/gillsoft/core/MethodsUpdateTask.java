package com.gillsoft.core;

import java.io.Serializable;

import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.util.ContextProvider;

public class MethodsUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = -264270290014365450L;
	
	private ResourceRequest request;

	public MethodsUpdateTask(ResourceRequest request) {
		this.request = request;
	}

	@Override
	public void run() {
		ResourceInfoController controller = ContextProvider.getBean(ResourceInfoController.class);
		controller.createCachedMethods(request);
	}

}
