package com.gillsoft.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface Description {
	
	default public String getDescription(Lang lang) {
		return Lang.getValue(getDescription(), lang);
    }
	
	default public void setDescription(String description) {
		setDescription(Lang.EN, description);
	}

	default public void setDescription(Lang lang, String description) {
        if (description == null) {
            return;
        }
        if (getDescription() == null) {
        	setDescription(new ConcurrentHashMap<>());
        }
        getDescription().put(lang, description);
    }
    
	public ConcurrentMap<Lang, String> getDescription();

	public void setDescription(ConcurrentMap<Lang, String> description);

}
