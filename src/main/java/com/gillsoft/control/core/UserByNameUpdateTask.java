package com.gillsoft.control.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.User;
import com.gillsoft.util.ContextProvider;

public class UserByNameUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = -5750023671147167649L;
	
	private String userName;
	
	public UserByNameUpdateTask() {
		
	}

	public UserByNameUpdateTask(String userName) {
		this.userName = userName;
	}

	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, getCacheKey());
		params.put(MemoryCacheHandler.IGNORE_AGE, true);
		params.put(MemoryCacheHandler.UPDATE_DELAY, 600000l);
		
		MsDataService service = ContextProvider.getBean(MsDataService.class);
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		try {
			User user = getUser(service);
			if (user == null) {
				user = (User) dataController.getCache().read(params);
			}
			params.put(MemoryCacheHandler.UPDATE_TASK, this);
			dataController.getCache().write(user, params);
		} catch (IOCacheException e) {
		}
	}
	
	protected String getCacheKey() {
		return MsDataController.getUserCacheKey(userName);
	}
	
	protected User getUser(MsDataService service) {
		return service.getUser(userName);
	}

}
