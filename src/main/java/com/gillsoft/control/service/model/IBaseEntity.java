package com.gillsoft.control.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.util.ContextProvider;

public interface IBaseEntity {
	
	public BaseEntity getBaseEntity();
	
	@JsonIgnore
	default public AttributeService getAttributeService() {
		return ContextProvider.getBean(AttributeService.class);
	}

}
