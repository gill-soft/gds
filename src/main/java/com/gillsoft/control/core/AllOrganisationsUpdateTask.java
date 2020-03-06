package com.gillsoft.control.core;

import java.io.Serializable;
import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.util.ContextProvider;

public class AllOrganisationsUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = -6401288933650846830L;

	public AllOrganisationsUpdateTask() {
		
	}
	
	@Override
	protected String getCacheKey() {
		return MsDataController.getAllOrganisationsCacheKey();
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		return dataController.createOrganisationsMap(getList(service));
	}
	
	protected List<Organisation> getList(MsDataService service) {
		return service.getAllOrganisations();
	}

}
