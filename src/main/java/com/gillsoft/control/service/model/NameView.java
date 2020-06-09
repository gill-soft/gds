package com.gillsoft.control.service.model;

import com.gillsoft.model.Lang;

public interface NameView extends IBaseEntity {
	
	public static final String ATTRIBUTE_NAME = "name";
	
	default public String getName() {
		String name = getAttributeService().getValue(ATTRIBUTE_NAME, getBaseEntity());
		if (name == null
				|| name.isEmpty()) {
			for (Lang lang : Lang.values()) {
				name = getName(lang);
				if (name != null
						&& !name.isEmpty()) {
					return name;
				}
			}
		}
		return name;
	}
	
	default public void setName(String name) {
		getAttributeService().addAttributeValue(ATTRIBUTE_NAME, name, getBaseEntity());
	}
	
	default public String getName(Lang lang) {
		return getAttributeService().getValue(ATTRIBUTE_NAME + "_" + lang, getBaseEntity());
	}
	
	default public void setName(Lang lang, String name) {
		getAttributeService().addAttributeValue(ATTRIBUTE_NAME + "_" + lang, name, getBaseEntity());
	}

}
