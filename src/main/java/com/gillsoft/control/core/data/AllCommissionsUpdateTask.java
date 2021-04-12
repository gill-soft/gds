package com.gillsoft.control.core.data;

import java.io.Serializable;
import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.util.ContextProvider;

public class AllCommissionsUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = 9204052080494610361L;

	public AllCommissionsUpdateTask() {
		
	}
	
	@Override
	protected String getCacheKey() {
		return MsDataController.getAllCommissionsKey();
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		return dataController.toMap(getList(service));
	}
	
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllCommissions();
	}

}
