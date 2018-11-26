package com.gillsoft.control.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.User;
import com.gillsoft.util.ContextProvider;

public class UserUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = -5750023671147167649L;
	
	private String userName;
	
	public UserUpdateTask(String userName) {
		this.userName = userName;
	}

	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, MsDataController.getUserCacheKey(userName));
		params.put(MemoryCacheHandler.IGNORE_AGE, true);
		params.put(MemoryCacheHandler.UPDATE_DELAY, 600000l);
		
		MsDataService service = ContextProvider.getBean(MsDataService.class);
		MsDataController dataController = ContextProvider.getBean(MsDataController.class);
		try {
			User user = service.getUser(userName);
			if (user == null) {
				user = (User) dataController.getCache().read(params);
			}
			params.put(MemoryCacheHandler.UPDATE_TASK, this);
			dataController.getCache().write(user, params);
		} catch (IOCacheException e) {
		}
	}

}
