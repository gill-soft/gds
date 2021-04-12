package com.gillsoft.control.core.data;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;

public class UserByIdUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = -7311722861124856999L;
	
	private long id;
	
	public UserByIdUpdateTask() {
		
	}

	public UserByIdUpdateTask(long id) {
		this.id = id;
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getUserCacheKey(id);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		return service.getUser(id);
	}

}
