package com.gillsoft.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.entity.Resource;
import com.gillsoft.service.MsDataService;
import com.gillsoft.util.ContextProvider;

public class ActiveResourcesUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = 555763368848572830L;
	
	private String userName;
	
	public ActiveResourcesUpdateTask(String userName) {
		this.userName = userName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, MsDataController.getActiveResourcesCacheKey(userName));
		params.put(MemoryCacheHandler.IGNORE_AGE, true);
		params.put(MemoryCacheHandler.UPDATE_DELAY, 1800000l);
		
		MsDataService service = ContextProvider.getBean(MsDataService.class);
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		try {
			List<Resource> resources = service.getUserResources(userName);
			if (resources == null) {
				resources = (List<Resource>) dataController.getCache().read(params);
			}
			params.put(MemoryCacheHandler.UPDATE_TASK, this);
			dataController.getCache().write(resources, params);
		} catch (IOCacheException e) {
		}
	}

}
