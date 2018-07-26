package com.gillsoft.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = -4445951612165534860L;
	
	protected void writeNotUpdatedObject(CacheHandler cache, String key, Object cachedObject, long timeToLive) {
		writeObject(cache, key, cachedObject, timeToLive, 0, false, true);
	}
	
	protected void writeObjectIgnoreAge(CacheHandler cache, String key, Object cachedObject, long updateDelay) {
		writeObject(cache, key, cachedObject, 0, updateDelay, true, false);
	}
	
	protected void writeObject(CacheHandler cache, String key, Object cachedObject, long timeToLive, long updateDelay) {
		writeObject(cache, key, cachedObject, timeToLive, updateDelay, false, false);
	}

	protected void writeObject(CacheHandler cache, String key, Object cachedObject, long timeToLive, long updateDelay,
			boolean ignoreAge, boolean disableUpdate) {
		if (cachedObject != null) {
			Map<String, Object> params = new HashMap<>();
			params.put(RedisMemoryCache.OBJECT_NAME, key);
			
			// время жизни кэша
			params.put(RedisMemoryCache.TIME_TO_LIVE, timeToLive);
			
			// как часто обновлять
			params.put(RedisMemoryCache.UPDATE_DELAY, updateDelay);
			
			if (ignoreAge) {
				params.put(RedisMemoryCache.IGNORE_AGE, Boolean.TRUE);
			}
			if (!disableUpdate) {
				params.put(RedisMemoryCache.UPDATE_TASK, this);
			}
			try {
				cache.write(cachedObject, params);
			} catch (IOCacheException e) {
			}
		}
	}

}
