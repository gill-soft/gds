package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.model.Segment;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.User;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MsDataController {
	
	private static final String ACTIVE_RESOURCES_CACHE_KEY = "active.resources.";
	
	private static final String ALL_COMMISSIONS_KEY = "all.commissions";
	
	private static final String USER_KEY = "user.";
	
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
		String userName = authentication.getName();
//		return (List<Resource>) getFromCache(getActiveResourcesCacheKey(userName),
//				new UserResourcesUpdateTask(userName), () -> new CopyOnWriteArrayList<>(msService.getUserResources(userName)), 1800000l);
		Resource resource = new Resource();
		resource.setId(108);
		resource.setHost("http://localhost:8080/ecolines");
		return Collections.singletonList(resource);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public Map<Long, List<Commission>> getAllCommissions() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<Commission>>) getFromCache(getAllCommissionsKey(),
				new AllCommissionsUpdateTask(), () -> toMap(msService.getAllCommissions()), 1800000l);
	}
	
	public Map<Long, List<Commission>> toMap(List<Commission> commissions) {
		if (commissions != null) {
			
			// entity id -> list of commissions
			return commissions.stream().collect(Collectors.groupingByConcurrent(
					c -> c.getParents().iterator().next().getId(), Collectors.toCollection(CopyOnWriteArrayList::new)));
		} else {
			return null;
		}
	}
	
	public User getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		String userName = authentication.getName();
		return (User) getFromCache(getUserCacheKey(userName),
				new UserUpdateTask(userName), () -> msService.getUser(userName), 600000l);
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
	
	public List<Commission> getCommissions(Segment segment) {
		List<Commission> commissions = new ArrayList<>();
		// TODO
		User user = getUser();
		if (user != null) {
			Map<Long, List<Commission>> allCommissions = getAllCommissions();
			if (allCommissions != null) {
				
			}
		}
		return commissions;
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
	
	public static String getUserCacheKey(String userName) {
		return USER_KEY + userName;
	}
	
	private interface CacheObjectGetter {
		
		public Object forCache();
		
	}

}
