package com.gillsoft.control.core;

import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;

public class AllTicketLayoutsUpdateTask extends AllCommissionsUpdateTask {

	private static final long serialVersionUID = -4167848828012193657L;

	public AllTicketLayoutsUpdateTask() {
		
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getAllTicketLayoutsKey();
	}

	@Override
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllTicketLayouts();
	}
	
}
