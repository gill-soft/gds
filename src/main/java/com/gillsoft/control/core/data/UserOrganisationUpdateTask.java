package com.gillsoft.control.core.data;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;

public class UserOrganisationUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = -7636264100113220122L;
	
	private String userName;
	
	public UserOrganisationUpdateTask() {
		
	}

	public UserOrganisationUpdateTask(String userName) {
		this.userName = userName;
	}
	
	@Override
	protected String getCacheKey() {
		return MsDataController.getUserOrganisationCacheKey(userName);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		return service.getUserOrganisation(userName);
	}

}
