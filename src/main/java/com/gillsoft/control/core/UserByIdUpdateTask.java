package com.gillsoft.control.core;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.User;

public class UserByIdUpdateTask extends UserByNameUpdateTask {

	private static final long serialVersionUID = -7311722861124856999L;
	
	private long id;
	
	public UserByIdUpdateTask(long id) {
		this.id = id;
	}

	protected String getCacheKey() {
		return MsDataController.getUserCacheKey(id);
	}
	
	protected User getUser(MsDataService service) {
		return service.getUser(id);
	}

}
