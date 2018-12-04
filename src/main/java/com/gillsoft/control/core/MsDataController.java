package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
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
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ValueType;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.ms.entity.BaseEntity;
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
	
	public ResourceParams createResourceParams(long resourceId) {
		List<Resource> resources = getUserResources();
		if (resources != null) {
			for (Resource resource : resources) {
				if (resource.getId() == resourceId) {
					return resource.createParams();
				}
			}
		}
		return null;
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
			Map<Long, List<Commission>> grouping = new ConcurrentHashMap<>();
			for (Commission commission : commissions) {
				commission.setParents(new CopyOnWriteArraySet<>(commission.getParents()));
				for (BaseEntity parent : commission.getParents()) {
					List<Commission> groupe = grouping.get(parent.getId());
					if (groupe == null) {
						grouping.put(parent.getId(), new CopyOnWriteArrayList<>());
					} else {
						groupe.add(commission);
					}
				}
			}
			return grouping;
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
	
	public Collection<com.gillsoft.model.Commission> getCommissions(Segment segment) {
		User user = getUser();
		if (user != null) {
			List<BaseEntity> entities = new ArrayList<>();
			entities.add(user);
			BaseEntity parent = user;
			while ((parent = (parent.getParents() != null && parent.getParents().iterator().hasNext()
					? parent.getParents().iterator().next() : null)) != null) {
				entities.add(parent);
			}
			// TODO add segment object's ids
			
			return getCommissions(entities);
		}
		return null;
	}
	
	public Collection<com.gillsoft.model.Commission> getCommissions(List<BaseEntity> entities) {
		Map<Long, List<Commission>> allCommissions = getAllCommissions();
		if (allCommissions != null) {
			
			// нужны только уникальные комиссии
			Map<Long, Commission> commissions = new HashMap<>();
			for (BaseEntity entity : entities) {
				List<Commission> entityCommissions = allCommissions.get(entity.getId());
				if (entityCommissions != null) {
					long currTime = System.currentTimeMillis();
					
					// отбираем только действующие комиссии
					commissions.putAll(entityCommissions.stream().filter(c -> 
							(c.getStart() == null || c.getStart().getTime() <= currTime)
							&& (c.getEnd() == null || c.getEnd().getTime() >= currTime))
							.collect(Collectors.toMap(Commission::getId, c -> c, (c1, c2) -> c1)));
				}
			}
			if (!commissions.isEmpty()) {
				
				// выбираем комиссии, у которых все паренты есть в переданном списке
				Map<String, Commission> result = new HashMap<>();
				Set<Long> entityIds = entities.stream().map(BaseEntity::getId).collect(Collectors.toSet());
				for (Commission commission : commissions.values()) {
					Set<Long> parentIds = commission.getParents().stream().map(BaseEntity::getId).collect(Collectors.toSet());
					if (entityIds.containsAll(parentIds)) {
						
						// берем комиссии с одинаковым кодом и оставляем только те, у которых больше веса всех родителей
						Commission compared = result.get(commission.getCode());
						if (compared == null
								|| getWeight(commission) > getWeight(compared)) {
							result.put(commission.getCode(), commission);
						}
					}
				}
				// конвертируем из комиссий базы в комиссии апи gds-commons
				return result.values().stream().map(c -> convert(c)).collect(Collectors.toList());
			}
		}
		return null;
	}
	
	private com.gillsoft.model.Commission convert(Commission commission) {
		com.gillsoft.model.Commission converted = new com.gillsoft.model.Commission();
		converted.setCode(commission.getCode());
		converted.setValue(commission.getValue());
		converted.setValueCalcType(CalcType.valueOf(commission.getValueCalcType().name()));
		converted.setVat(commission.getVat());
		converted.setVatCalcType(CalcType.valueOf(commission.getVatCalcType().name()));
		converted.setType(ValueType.valueOf(commission.getVatType().name()));
		converted.setCurrency(Currency.valueOf(commission.getCurrency().name()));
		return converted;
	}
	
	private int getWeight(Commission commission) {
		return commission.getParents().stream().mapToInt(e -> e.getType().getWeight()).sum();
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
