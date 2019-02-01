package com.gillsoft.control.core;

import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;

public class AllFiltersUpdateTask extends AllCommissionsUpdateTask {

	private static final long serialVersionUID = 970236571354075432L;
	
	public AllFiltersUpdateTask() {
		super();
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getAllFiltersKey();
	}
	
	@Override
	protected List<? extends BaseEntity> getCachedList(MsDataService service) {
		return service.getAllFilters();
	}

}
