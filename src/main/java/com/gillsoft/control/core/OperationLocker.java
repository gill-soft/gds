package com.gillsoft.control.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.control.api.OperationLockedException;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OperationLocker {
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	public String lock(long orderId) {
		checkLock(orderId, null);
		
		String lockId = StringUtil.generateUUID();
		Map<String, Object> params = getParams(orderId);
		try {
			cache.write(lockId, params);
		} catch (IOCacheException e) {
		}
		return lockId;
	}
	
	private Map<String, Object> getParams(long orderId) {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, String.valueOf(orderId));
		params.put(RedisMemoryCache.TIME_TO_LIVE, 300000l);
		return params;
	}
	
	public void unlock(long orderId) {
		try {
			cache.write(null, getParams(orderId));
		} catch (IOCacheException e) {
		}
	}
	
	public void checkLock(long orderId, String lockId) {
		try {
			Object res = cache.read(getParams(orderId));
			if (res != null
					&& (lockId == null
							|| !Objects.equals(lockId, (String) res))) {
				throw new OperationLockedException(
						"Operation is locked for selected order in current time. Wait the result of previous operation and try again.");
			}
		} catch (IOCacheException e) {
			// если ошибка чтения, то блокировки нет
		}
	}

}
