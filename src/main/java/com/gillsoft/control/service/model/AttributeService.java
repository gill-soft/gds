package com.gillsoft.control.service.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.ms.entity.Attribute;
import com.gillsoft.ms.entity.AttributeValue;
import com.gillsoft.ms.entity.BaseEntity;

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class AttributeService {
	
	@Autowired
	private MsDataService msService;
	
	@Autowired
    @Qualifier("MemoryCacheHandler")
	private CacheHandler cache;
	
	public void addAttributeValue(String name, String value, BaseEntity entity) {
		AttributeValue attributeValue = getAttributeValue(name, entity);
		if (attributeValue != null) {
			attributeValue.setValue(value);
		} else {
			Attribute attribute = getAttributeByName(name);
			if (attribute != null) {
				attributeValue = new AttributeValue();
				attributeValue.setAttribute(attribute);
				attributeValue.setValue(value);
				entity.getAttributeValues().add(attributeValue);
			}
		}
	}
	
	public String getValue(String name, BaseEntity entity) {
		AttributeValue value = getAttributeValue(name, entity);
		if (value != null) {
			return value.getValue();
		}
		return null;
	}
	
	public AttributeValue getAttributeValue(String name, BaseEntity entity) {
		for (AttributeValue value : entity.getAttributeValues()) {
			if (value.getAttribute() != null
					&& name.equals(value.getAttribute().getName())) {
				return value;
			}
		}
		return null;
	}
	
	public Attribute getAttributeByName(String name) {
		Map<String, Attribute> attributes = getAttributes();
		if (attributes != null) {
			return attributes.get(name);
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Attribute> getAttributes() {
		
		// берем результат с кэша, если кэша нет, то берем напрямую с сервиса
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, "attributes");
		try {
			Map<String, Attribute> attributes = (Map<String, Attribute>) cache.read(params);
			if (attributes == null) {
				
				// синхронизация по ключу кэша
				synchronized (AttributeService.class) {
					attributes = (Map<String, Attribute>) cache.read(params);
					if (attributes == null) {
						attributes = createAttributesMap();
						params.put(MemoryCacheHandler.IGNORE_AGE, true);
						params.put(MemoryCacheHandler.UPDATE_DELAY, 1800000l);
						params.put(MemoryCacheHandler.UPDATE_TASK, new AttributesUpdateTask());
						try {
							cache.write(attributes, params);
						} catch (IOCacheException writeException) {
						}
					}
					return attributes;
				}
			} else {
				return attributes;
			}
		} catch (IOCacheException e) {
			return null;
		}
	}
	
	private Map<String, Attribute> createAttributesMap() {
		List<Attribute> all = msService.getAllAttributes();
		if (all != null) {
			return all.stream().collect(Collectors.toConcurrentMap(Attribute::getName, a -> a));
		}
		return null;
	}
	
	private class AttributesUpdateTask implements Runnable {

		@Override
		public void run() {
			Map<String, Attribute> attributes = createAttributesMap();
			if (attributes == null) {
				return;
			}
			Map<String, Object> params = new HashMap<>();
			params.put(MemoryCacheHandler.OBJECT_NAME, "attributes");
			params.put(MemoryCacheHandler.IGNORE_AGE, true);
			params.put(MemoryCacheHandler.UPDATE_DELAY, 1800000l);
			params.put(MemoryCacheHandler.UPDATE_TASK, this);
			try {
				cache.write(attributes, params);
			} catch (IOCacheException writeException) {
			}
		}
		
	}

}
