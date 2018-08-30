package com.gillsoft.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface Name {
	
	default public String getName(Lang lang) {
        return Lang.getValue(getName(), lang);
    }
	
	default public void setName(String name) {
		setName(Lang.EN, name);
	}

	default public void setName(Lang lang, String name) {
        if (name == null) {
            return;
        }
        if (getName() == null) {
        	setName(new ConcurrentHashMap<>());
        }
        getName().put(lang, name);
    }
    
	public ConcurrentMap<Lang, String> getName();

	public void setName(ConcurrentMap<Lang, String> name);

}
