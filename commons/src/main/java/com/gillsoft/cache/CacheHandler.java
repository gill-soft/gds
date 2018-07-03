package com.gillsoft.cache;

import java.util.Map;

public interface CacheHandler {
	
	/**
	 * Наименование-ключ объекта в кэше.
	 */
	public static String OBJECT_NAME = "objectName";
	
	public void write(Object storedObject, Map<String, Object> params) throws IOCacheException;
	
	public Object read(Map<String, Object> params) throws IOCacheException;

}
