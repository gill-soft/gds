package com.gillsoft.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.model.response.TripSearchResponse;

public abstract class CacheUtil {
	
	/**
	 * Кладет в кэш на 1 минуту выполняющиеся задания поиска рейсов и возвращает
	 * TripSearchResponse с ид записи в кэше.
	 * @param <V>
	 * 
	 * @param cache
	 *            Используемый кэш.
	 * @param futures
	 *            Список заданий.
	 * @return Ответ с ид записи в кэше.
	 */
	public static <T> TripSearchResponse putToCache(CacheHandler cache, List<Future<T>> futures) {
		String searchId = StringUtil.generateUUID();
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		params.put(MemoryCacheHandler.TIME_TO_LIVE, 60000l);
		try {
			cache.write(futures, params);
			return new TripSearchResponse(null, searchId);
		} catch (IOCacheException e) {
			return new TripSearchResponse(null, e);
		}
	}
	
	public static Object getFromCache(CacheHandler cache, String searchId) throws IOCacheException {
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		Object value = cache.read(params);
		if (value == null) {
			throw new IOCacheException("Too late for getting result");
		} else {
			return value;
		}
	}

}
