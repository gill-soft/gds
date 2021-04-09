package com.gillsoft.control.core;

import java.io.Serializable;
import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.util.ContextProvider;

public class AllAdditionalServicesUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = 7087981656890811396L;

	public AllAdditionalServicesUpdateTask() {
		
	}
	
	@Override
	protected String getCacheKey() {
		return MsDataController.getAllAdditionalServicesKey();
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		return dataController.toMap(getList(service));
	}
	
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllAdditionalServices();
	}

}
