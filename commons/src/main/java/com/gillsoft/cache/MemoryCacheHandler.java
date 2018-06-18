package com.gillsoft.cache;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MemoryCacheHandler implements CacheHandler, Runnable {
	
	public static final String UPDATE_TASK = "updateTask"; // задание обновления объекта в кэше
	public static final String TIME_TO_LIVE = "timeToLive"; // сколько будет жить объект в кэше
	public static final String UPDATE_DELAY = "updateDelay"; // как часто обновлять объект в кэше
	public static final String IGNORE_IS_READED = "ignoreIsReaded"; // обновлять объект независимо от чтения
	public static final String IGNORE_AGE = "ignoreAge"; // не удалять объект с кэша
	
	private ConcurrentMap<Object, CacheObject> cache = new ConcurrentHashMap<>();
	private ExecutorService executor = Executors.newFixedThreadPool(100);
	
	public MemoryCacheHandler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.initialize();
		scheduler.scheduleWithFixedDelay(this, 1000);
	}

	@Override
	public void write(Object storedObject, Map<String, Object> params)
			throws IOCacheException {
		CacheObject cacheObject = new CacheObject();
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
			cacheObject.setCreatedTime(System.currentTimeMillis() + (Long) timeToLive);
		}
		if (params.containsKey(IGNORE_IS_READED)) {
			cacheObject.setReaded(true);
		}
		if (params.containsKey(IGNORE_AGE)) {
			cacheObject.setEternal(true);
		}
		cache.put(params.get(OBJECT_NAME), cacheObject);
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
	
	@Override
	public void run() {
		
		// удаляем кэш, который старше TIME_TO_LIVE
		long curr = System.currentTimeMillis();
		long age = curr;
		for (Entry<Object, CacheObject> cacheEntry : cache.entrySet()) {
			CacheObject cacheObject = cacheEntry.getValue();
			if (cacheObject.getCreatedTime() <= age
					&& !cacheObject.isEternal()) {
				cache.remove(cacheEntry.getKey());
			} else if (cacheObject.isReaded()
					&& cacheObject.getCreatedTime() <= curr - cacheObject.getUpdateDelay()
					&& cacheObject.getUpdateTask() != null) {
				executor.execute(cacheObject.getUpdateTask());
			}
		}
	}
	
	private class CacheObject {
		
		private Object cachedObject;
		private boolean readed = false;
		private boolean eternal = false;
		private long createdTime = System.currentTimeMillis();
		private Runnable updateTask;
		private long updateDelay;
		
		public Object getCachedObject() {
			return cachedObject;
		}
		
		public void setCachedObject(Object cachedObject) {
			this.cachedObject = cachedObject;
		}
		
		public boolean isReaded() {
			return readed;
		}

		public void setReaded(boolean readed) {
			this.readed = readed;
		}

		public boolean isEternal() {
			return eternal;
		}

		public void setEternal(boolean eternal) {
			this.eternal = eternal;
		}

		public long getCreatedTime() {
			return createdTime;
		}
		
		public void setCreatedTime(long createdTime) {
			this.createdTime = createdTime;
		}

		public Runnable getUpdateTask() {
			return updateTask;
		}

		public void setUpdateTask(Runnable updateTask) {
			this.updateTask = updateTask;
		}

		public long getUpdateDelay() {
			return updateDelay;
		}

		public void setUpdateDelay(long updateDelay) {
			this.updateDelay = updateDelay;
		}
		
	}

}
