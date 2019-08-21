package com.gillsoft.control.core;

import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;

public class AllReturnConditionsUpdateTask extends AllCommissionsUpdateTask {

	private static final long serialVersionUID = 2393913592009105670L;

	public AllReturnConditionsUpdateTask() {
		super();
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getAllReturnConditionsKey();
	}

	@Override
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllReturnConditions();
	}
	
}
