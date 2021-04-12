package com.gillsoft.control.core.data;

import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;

public class AllConnectionDiscountsUpdateTask extends AllCommissionsUpdateTask {

	private static final long serialVersionUID = -827200624925554271L;

	public AllConnectionDiscountsUpdateTask() {
		
	}
	
	@Override
	protected String getCacheKey() {
		return MsDataController.getAllResourceConnectionDiscountsKey();
	}

	@Override
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllResourceConnectionDiscounts();
	}

}
