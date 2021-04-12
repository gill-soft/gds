package com.gillsoft.control.core.data;

import java.util.HashMap;
import java.util.Map;

import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.util.ContextProvider;

public abstract class MsDataObjectUpdateTask implements Runnable {
	
	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, getCacheKey());
		params.put(MemoryCacheHandler.IGNORE_AGE, true);
		params.put(MemoryCacheHandler.UPDATE_DELAY, 120000l);
		
		MsDataService service = ContextProvider.getBean(MsDataService.class);
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		try {
			Object dataObject = getDataObject(service);
			if (dataObject == null) {
				dataObject = dataController.getCache().read(params);
			}
			params.put(MemoryCacheHandler.UPDATE_TASK, this);
			dataController.getCache().write(dataObject, params);
		} catch (IOCacheException e) {
		}
	}
	
	protected abstract String getCacheKey();
	
	protected abstract Object getDataObject(MsDataService service);

}
