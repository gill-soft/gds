package com.gillsoft.control.core;

import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;

public class AllOrdersAccessUpdateTask extends AllCommissionsUpdateTask {

	private static final long serialVersionUID = -8997268935543892648L;

	public AllOrdersAccessUpdateTask() {
		
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getAllOrdersAccessKey();
	}

	@Override
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllOrdersAccess();
	}

}
