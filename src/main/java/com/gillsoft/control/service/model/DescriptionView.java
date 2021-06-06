package com.gillsoft.control.service.model;

import com.gillsoft.model.Lang;

public interface DescriptionView extends IBaseEntity {
	
	public static final String ATTRIBUTE_NAME = "description";
	
	default public String getDescription() {
		String description = getAttributeService().getValue(ATTRIBUTE_NAME, getBaseEntity());
		if (description == null
				|| description.isEmpty()) {
			for (Lang lang : Lang.values()) {
				description = getDescription(lang);
				if (description != null
						&& !description.isEmpty()) {
					return description;
				}
			}
		}
		return description;
	}
	
	default public void setDescription(String description) {
		getAttributeService().addAttributeValue(ATTRIBUTE_NAME, description, getBaseEntity());
	}
	
	default public String getDescription(Lang lang) {
		return getAttributeService().getValue(ATTRIBUTE_NAME + "_" + lang, getBaseEntity());
	}
	
	default public void setDescription(Lang lang, String description) {
		getAttributeService().addAttributeValue(ATTRIBUTE_NAME + "_" + lang, description, getBaseEntity());
	}

}
