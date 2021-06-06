package com.gillsoft.control.service.model;

public interface LanguageView extends IBaseEntity {
	
	default public String getLanguage() {
		return getAttributeService().getValue("language", getBaseEntity());
	}
	
	default public void setLanguage(String language) {
		getAttributeService().addAttributeValue("language", language, getBaseEntity());
	}

}
