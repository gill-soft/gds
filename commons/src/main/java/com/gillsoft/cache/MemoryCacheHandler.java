package com.gillsoft.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("MemoryCacheHandler")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MemoryCacheHandler implements CacheHandler {
	
	/**
	 * Задание обновления объекта в кэше.
	 */
	public static final String UPDATE_TASK = "updateTask";
	
	/**
	 * Сколько будет жить объект в кэше. Миллисекунды.
	 */
	public static final String TIME_TO_LIVE = "timeToLive";
	
	/**
	 * Как часто обновлять объект в кэше. Миллисекунды.
	 */
	public static final String UPDATE_DELAY = "updateDelay";
	
	/**
	 * Обновлять объект независимо от чтения.
	 */
	public static final String IGNORE_IS_READED = "ignoreIsReaded";
	
	/**
	 * Не удалять объект с кэша.
	 */
	public static final String IGNORE_AGE = "ignoreAge";
	
	protected ConcurrentMap<Object, CacheObject> cache = new ConcurrentHashMap<>();
	protected ExecutorService executor = Executors.newFixedThreadPool(100);
	
	public MemoryCacheHandler() {
		
	}
	
	public CacheObject createObject(Object storedObject, Map<String, Object> params) {
		CacheObject cacheObject = new CacheObject();
		cacheObject.setName(params.get(OBJECT_NAME).toString());
		cacheObject.setCachedObject(storedObject);
		Object updateTask = params.get(UPDATE_TASK);
		if (updateTask != null) {
			cacheObject.setUpdateTask((Runnable) updateTask);
		}
		Object updateDelay = params.get(UPDATE_DELAY);
		if (updateDelay != null) {
			cacheObject.setUpdateDelay((Long) updateDelay);
		}
		Object timeToLive = params.get(TIME_TO_LIVE);
		if (timeToLive != null) {
			cacheObject.setTimeToLive((Long) timeToLive);
		}
		if (params.containsKey(IGNORE_IS_READED)) {
			cacheObject.setReaded(true);
		}
		if (params.containsKey(IGNORE_AGE)) {
			cacheObject.setEternal(true);
		}
		return cacheObject;
	}

	@Override
	public void write(Object storedObject, Map<String, Object> params)
			throws IOCacheException {
		cache.put(params.get(OBJECT_NAME), createObject(storedObject, params));
	}

	@Override
	public Object read(Map<String, Object> params) throws IOCacheException {
		CacheObject cacheObject = cache.get(params.get(OBJECT_NAME));
		if (cacheObject != null) {
			cacheObject.setReaded(true);
			return cacheObject.getCachedObject();
		} else {
			return null;
		}
	}
	
	@PostConstruct
	@Scheduled(initialDelay = 10000, fixedDelay = 10000)
	public void updateCached() {
		
		// удаляем кэш, который старше TIME_TO_LIVE
		long curr = System.currentTimeMillis();
		for (Entry<Object, CacheObject> cacheEntry : cache.entrySet()) {
			CacheObject cacheObject = cacheEntry.getValue();
			if (cacheObject.getTimeToLive() <= curr
					&& !cacheObject.isEternal()) {
				cache.remove(cacheEntry.getKey());
			} else if (cacheObject.isReaded()
					&& cacheObject.getUpdateTask() != null
					&& cacheObject.getCreated() <= curr - cacheObject.getUpdateDelay()) {
				executor.execute(cacheObject.getUpdateTask());
			}
		}
	}

}
