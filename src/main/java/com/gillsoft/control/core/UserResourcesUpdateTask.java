package com.gillsoft.control.core;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;

public class UserResourcesUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = 555763368848572830L;
	
	private String userName;
	
	public UserResourcesUpdateTask() {
		
	}

	public UserResourcesUpdateTask(String userName) {
		this.userName = userName;
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getActiveResourcesCacheKey(userName);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		return service.getUserResources(userName);
	}

}
