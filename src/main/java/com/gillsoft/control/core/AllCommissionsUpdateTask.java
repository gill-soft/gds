package com.gillsoft.control.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.util.ContextProvider;

public class AllCommissionsUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = 9204052080494610361L;

	public AllCommissionsUpdateTask() {
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, getCacheKey());
		params.put(MemoryCacheHandler.IGNORE_AGE, true);
		params.put(MemoryCacheHandler.UPDATE_DELAY, 1800000l);
		
		MsDataService service = ContextProvider.getBean(MsDataService.class);
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		try {
			Map<Long, List<BaseEntity>> commissions = dataController.toMap(getCachedList(service));
			if (commissions == null) {
				commissions = (Map<Long, List<BaseEntity>>) dataController.getCache().read(params);
			}
			params.put(MemoryCacheHandler.UPDATE_TASK, this);
			dataController.getCache().write(commissions, params);
		} catch (IOCacheException e) {
		}
	}
	
	protected String getCacheKey() {
		return MsDataController.getAllCommissionsKey();
	}
	
	protected List<? extends BaseEntity> getCachedList(MsDataService service) {
		return service.getAllCommissions();
	}

}
