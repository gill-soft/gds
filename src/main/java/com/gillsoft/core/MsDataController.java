package com.gillsoft.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.entity.Commission;
import com.gillsoft.entity.Resource;
import com.gillsoft.service.MsDataService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MsDataController {
	
	private static final String ACTIVE_RESOURCES_CACHE_KEY = "active.resources.";
	
	private static final String ALL_COMMISSIONS_KEY = "all.commissions";
	
	@Autowired
	private MsDataService msService;
	
	@Autowired
    @Qualifier("MemoryCacheHandler")
	private CacheHandler cache;
	
	@SuppressWarnings("unchecked")
	public List<Resource> getUserResources() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		return (List<Resource>) getFromCache(getActiveResourcesCacheKey(authentication.getName()),
				new UserResourcesUpdateTask(authentication.getName()), () -> msService.getUserResources(authentication.getName()), 1800000l);
	}
	
	@SuppressWarnings("unchecked")
	public List<Commission> getAllCommissions() {
		return (List<Commission>) getFromCache(getAllCommissionsKey(),
				new AllCommissionsUpdateTask(), () -> msService.getAllCommissions(), 1800000l);
	}
	
	private Object getFromCache(String cacheKey, Runnable updateTask, CacheObjectGetter objectGetter, long updateDelay) {
		
		// берем результат с кэша, если кэша нет, то берем напрямую с сервиса
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, cacheKey);
		try {
			return cache.read(params);
		} catch (IOCacheException readException) {
			
			// синхронизация по ключу кэша
			synchronized (cacheKey.intern()) {
				Object object = objectGetter.forCache();
				params.put(MemoryCacheHandler.IGNORE_AGE, true);
				params.put(MemoryCacheHandler.UPDATE_DELAY, updateDelay);
				params.put(MemoryCacheHandler.UPDATE_TASK, updateTask);
				try {
					cache.write(object, params);
				} catch (IOCacheException writeException) {
				}
				return object;
			}
		}
	}
	
	public CacheHandler getCache() {
		return cache;
	}

	public static String getActiveResourcesCacheKey(String userName) {
		return ACTIVE_RESOURCES_CACHE_KEY + userName;
	}

	public static String getAllCommissionsKey() {
		return ALL_COMMISSIONS_KEY;
	}
	
	private interface CacheObjectGetter {
		
		public Object forCache();
		
	}

}
