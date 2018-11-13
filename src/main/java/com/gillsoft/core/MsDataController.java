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
import com.gillsoft.entity.Resource;
import com.gillsoft.service.MsDataService;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MsDataController {
	
	private static final String ACTIVE_RESOURCES_CACHE_KEY = "active.resources.";
	
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
		// берем результат с кэша, если кэша нет, то берем напрямую с сервиса
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, getActiveResourcesCacheKey(authentication.getName()));
		try {
			return (List<Resource>) cache.read(params);
		} catch (IOCacheException readException) {
			List<Resource> resources = msService.getUserResources(authentication.getName());
			params.put(MemoryCacheHandler.IGNORE_AGE, true);
			params.put(MemoryCacheHandler.UPDATE_DELAY, 1800000l);
			params.put(MemoryCacheHandler.UPDATE_TASK, new ActiveResourcesUpdateTask(authentication.getName()));
			try {
				cache.write(resources, params);
			} catch (IOCacheException writeException) {
			}
			return resources;
		}
	}
	
	public CacheHandler getCache() {
		return cache;
	}

	public static String getActiveResourcesCacheKey(String userName) {
		return ACTIVE_RESOURCES_CACHE_KEY + userName;
	}

}
