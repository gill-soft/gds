package com.gillsoft.cache;

import java.util.Map;

public interface CacheHandler {
	
	public static String RESOURCE_CODE = "resourceCode";
	
	public static String OBJECT_NAME = "objectName";
	
	public void write(Object storedObject, Map<String, Object> params) throws IOCacheException;
	
	public Object read(Map<String, Object> params) throws IOCacheException;

}
