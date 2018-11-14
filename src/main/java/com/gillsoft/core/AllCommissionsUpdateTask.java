package com.gillsoft.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.entity.Commission;
import com.gillsoft.service.MsDataService;
import com.gillsoft.util.ContextProvider;

public class AllCommissionsUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = 9204052080494610361L;

	public AllCommissionsUpdateTask() {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, MsDataController.getAllCommissionsKey());
		params.put(MemoryCacheHandler.IGNORE_AGE, true);
		params.put(MemoryCacheHandler.UPDATE_DELAY, 1800000l);
		
		MsDataService service = ContextProvider.getBean(MsDataService.class);
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		try {
			List<Commission> commissions = service.getAllCommissions();
			if (commissions == null) {
				commissions = (List<Commission>) dataController.getCache().read(params);
			}
			params.put(MemoryCacheHandler.UPDATE_TASK, this);
			dataController.getCache().write(commissions, params);
		} catch (IOCacheException e) {
		}
	}

}
