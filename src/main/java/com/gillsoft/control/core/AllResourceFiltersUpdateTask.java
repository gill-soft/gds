package com.gillsoft.control.core;

import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;

public class AllResourceFiltersUpdateTask extends AllCommissionsUpdateTask {

	private static final long serialVersionUID = -6064645806390654042L;

	public AllResourceFiltersUpdateTask() {
		
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getAllResourceFiltersKey();
	}

	@Override
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllResourceFilters();
	}

}
