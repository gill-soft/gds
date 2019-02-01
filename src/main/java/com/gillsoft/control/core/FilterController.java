package com.gillsoft.control.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gillsoft.model.Segment;
import com.gillsoft.ms.entity.CodeEntity;
import com.gillsoft.ms.entity.ServiceFilter;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@EnableScheduling
public class FilterController {
	
	private static Logger LOGGER = LogManager.getLogger(FilterController.class);
	
	@Autowired
	private MsDataController dataController;
	
	private Map<String, Field> fields;
	
	@PostConstruct
	@Scheduled(initialDelay = 600000, fixedDelay = 600000)
	public void updateFields() {
		Map<String, Field> fieldsMap = new HashMap<>();
		Map<Long, List<CodeEntity>> entities = dataController.getAllFilters();
		if (entities != null) {
			for (List<CodeEntity> filters : entities.values()) {
				for (CodeEntity entity : filters) {
					ServiceFilter filter = (ServiceFilter) entity;
					String[] name = filter.getFilteredField().split("\\.");
					try {
						Field[] fields = Class.forName("com.gillsoft.model." + name[0]).getDeclaredFields();
						for (int i = 1; i < name.length; i++) {
							Field field = getField(name[i], fields);
							if (field != null
									&& name.length > i) {
								fields = field.getType().getDeclaredFields();
								field.setAccessible(true);
								fieldsMap.put(getKey(name, i), field);
							}
						}
					} catch (SecurityException | ClassNotFoundException e) {
						LOGGER.error("Can not get class com.gillsoft.model." + name[0], e);
					}
				}
			}
		}
		this.fields = fieldsMap;
	}
	
	public String getKey(String[] name, int i) {
		return String.join(".", Arrays.copyOf(name, i + 1));
	}
	
	public Field getField(String name, Field[] fields) {
		for (Field field : fields) {
			Annotation[] annotations = field.getAnnotations();
			String fieldName = field.getName();
			if (Objects.equals(fieldName, name)) {
				return field;
			}
			for (Annotation annotation : annotations) {
				if (Objects.equals(JsonProperty.class.getName(), annotation.annotationType().getName())) {
					try {
						fieldName = (String) annotation.annotationType().getMethod("value").invoke(annotation);
						if (Objects.equals(fieldName, name)) {
							return field;
						}
					} catch (Exception e) {
					}
				}
			}
		}
		return null;
	}
	
	public void apply(Map<String, Segment> segments) {
		List<ServiceFilter> filters = dataController.getFilters();
		segments.keySet().removeIf(key -> isRemove(segments.get(key), filters));
	}
	
	private boolean isRemove(Segment segment, List<ServiceFilter> filters) {
		boolean remove = false;
		for (ServiceFilter filter : filters) {
			boolean currFilter = false;
			String[] name = filter.getFilteredField().split("\\.");
			Object value = segment;
			for (int i = 1; i < name.length; i++) {
				Field field = fields.get(getKey(name, i));
				if (field == null) {
					currFilter = true;
					break;
				}
				try {
					value = field.get(value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					LOGGER.error("Can not get value of field " + name[0], e);
					currFilter = true;
					break;
				}
				if (value == null) {
					currFilter = true;
					break;
				}
			}
			if (value != null) {
				boolean accepted = isAccepted(value.toString().toLowerCase(), filter);
				switch (filter.getFilterType()) {
				case INCLUDE:
					currFilter = !accepted;
					break;
				case EXCLUDE:
					currFilter = accepted;
					break;
				}
			}
			remove = remove || currFilter;
		}
		return remove;
	}
	
	private boolean isAccepted(String strValue, ServiceFilter filter) {
		switch (filter.getComparison()) {
		case EQUAL:
			return compare(strValue, filter.getValue()) == 0;
		case NOT_EQUAL:
			return compare(strValue, filter.getValue()) != 0;
		case GREATE:
			return compare(strValue, filter.getValue()) > 0;				
		case GREATE_EQUAL:
			return compare(strValue, filter.getValue()) >= 0;
		case LESS:
			return compare(strValue, filter.getValue()) < 0;
		case LESS_EQUAL:
			return compare(strValue, filter.getValue()) <= 0;
		case REGEX:
			return strValue.matches(filter.getValue());
		default:
			return true;
		}
	}
	
	private int compare(String value, String compared) {
		try {
			BigDecimal numValue = new BigDecimal(value);
			BigDecimal numCompared = new BigDecimal(compared);
			return numValue.compareTo(numCompared);
		} catch (Exception e) {
			return value.compareTo(compared);
		}
	}

}
