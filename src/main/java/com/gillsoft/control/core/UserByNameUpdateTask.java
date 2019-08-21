package com.gillsoft.control.core;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;

public class UserByNameUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = -5750023671147167649L;
	
	private String userName;
	
	public UserByNameUpdateTask() {
		
	}

	public UserByNameUpdateTask(String userName) {
		this.userName = userName;
	}
	
	@Override
	protected String getCacheKey() {
		return MsDataController.getUserCacheKey(userName);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		return service.getUser(userName);
	}

}
