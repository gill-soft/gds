package com.gillsoft.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface Title {
	
	default public String getTitle(Lang lang) {
		return Lang.getValue(getTitle(), lang);
    }
	
	default public void setTitle(String title) {
		setTitle(Lang.EN, title);
	}

	default public void setTitle(Lang lang, String title) {
        if (title == null) {
            return;
        }
        if (getTitle() == null) {
        	setTitle(new ConcurrentHashMap<>());
        }
        getTitle().put(lang, title);
    }
    
	public ConcurrentMap<Lang, String> getTitle();

	public void setTitle(ConcurrentMap<Lang, String> title);

}
