package com.gillsoft.control.core.data;

import java.util.List;

import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;

public class AllResourceConnectionsUpdateTask extends AllCommissionsUpdateTask {

	private static final long serialVersionUID = 304217145276204703L;

	public AllResourceConnectionsUpdateTask() {
		
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getAllResourceConnectionsKey();
	}

	@Override
	protected List<? extends BaseEntity> getList(MsDataService service) {
		return service.getAllResourceConnections();
	}

}
