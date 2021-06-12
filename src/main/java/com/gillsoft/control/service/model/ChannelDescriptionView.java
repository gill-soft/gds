package com.gillsoft.control.service.model;

import com.gillsoft.model.Lang;

public interface ChannelDescriptionView extends IBaseEntity {
	
	public static final String ATTRIBUTE_NAME = "description";
	
	default public String getDescription(String channel, Lang lang) {
		return getAttributeService().getValue(ATTRIBUTE_NAME + "_" + channel + "_" + lang, getBaseEntity());
	}
	
	default public void setDescription(String channel, Lang lang, String description) {
		getAttributeService().addAttributeValue(ATTRIBUTE_NAME + "_" + channel + "_" + lang, description, getBaseEntity());
	}

}
