package com.gillsoft.control.core;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;

public class OrganisationUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = -843859186013342127L;
	
	private long id;

	public OrganisationUpdateTask() {
		
	}

	public OrganisationUpdateTask(long id) {
		this.id = id;
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getOrganisationCacheKey(id);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		return service.getOrganisation(id);
	}

}
