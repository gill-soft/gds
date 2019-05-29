package com.gillsoft.control.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.AgregatorResourceInfoService;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.ResourceMethodResponse;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResourceInfoController {
	
	private static Logger LOGGER = LogManager.getLogger(ResourceInfoController.class);
	
	private static final String ACTIVE_RESOURCES_CACHE_KEY = "methods.resource.";
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	@Autowired
	private AgregatorResourceInfoService service;
	
	public boolean isMethodAvailable(Resource resource, String methodPath, MethodType methodType) {
		List<Method> methods = getAvailableMethods(resource);
		if (methods == null) {
			return false;
		}
		return methods.stream().anyMatch(m -> m.getType() == methodType && m.getUrl().equals(methodPath));
	}
	
	@SuppressWarnings("unchecked")
	public List<Method> getAvailableMethods(Resource resource) {
		
		// берем результат с кэша, если кэша нет, то берем напрямую с сервиса
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, getActiveResourcesCacheKey(resource.getId()));
		try {
			return (List<Method>) cache.read(params);
		} catch (IOCacheException readException) {
			return createCachedMethods(createRequest(resource));
		}
	}
	
	protected List<Method> createCachedMethods(ResourceRequest request) {
		String key = getActiveResourcesCacheKey(Long.parseLong(request.getParams().getResource().getId()));
		
		// синхронизируем выгрузку методов по ресурсу
		synchronized (key.intern()) {
			Map<String, Object> params = new HashMap<>();
			params.put(MemoryCacheHandler.OBJECT_NAME, key);
			List<ResourceMethodResponse> response = service.getAvailableMethods(Collections.singletonList(request));
			List<Method> methods = null;
			if (response == null
					|| response.isEmpty()
					|| Utils.isError(LOGGER, response.get(0))
					|| !request.getId().equals(response.get(0).getId())) {
				params.put(MemoryCacheHandler.UPDATE_DELAY, 300000l);
			} else {
				methods = response.get(0).getMethods();
				params.put(MemoryCacheHandler.UPDATE_DELAY, 1800000l);
			}
			params.put(MemoryCacheHandler.IGNORE_AGE, true);
			params.put(MemoryCacheHandler.UPDATE_TASK, new MethodsUpdateTask(request));
			try {
				cache.write(methods, params);
			} catch (IOCacheException writeException) {
			}
			return methods;
		}
	}
	
	// создание запроса получения информации о ресурсе 
	private ResourceRequest createRequest(Resource resource) {
		ResourceRequest request = new ResourceRequest();
		request.setId(StringUtil.generateUUID());
		request.setParams(resource.createParams());
		return request;
	}

	public CacheHandler getCache() {
		return cache;
	}

	public static String getActiveResourcesCacheKey(long resourceId) {
		return ACTIVE_RESOURCES_CACHE_KEY + resourceId;
	}

}
