package com.gillsoft.control.core;

import java.io.Serializable;
import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.ResourceParams;
import com.gillsoft.util.ContextProvider;

public class AllResourceParamsUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = 1068811324633728232L;

	public AllResourceParamsUpdateTask() {
		
	}
	
	@Override
	protected String getCacheKey() {
		return MsDataController.getAllResourceParamsCacheKey();
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		return dataController.createEntitiesMap(getList(service));
	}
	
	protected List<ResourceParams> getList(MsDataService service) {
		return service.getAllResourceParamsWithParent();
	}

}
