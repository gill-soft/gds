package com.gillsoft.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface Address {
	
	default public String getAddress(Lang lang) {
		return Lang.getValue(getAddress(), lang);
    }
	
	default public void setAddress(String address) {
		setAddress(Lang.EN, address);
	}

	default public void setAddress(Lang lang, String address) {
        if (address == null) {
            return;
        }
        if (getAddress() == null) {
        	setAddress(new ConcurrentHashMap<>());
        }
        getAddress().put(lang, address);
    }
    
	public ConcurrentMap<Lang, String> getAddress();

	public void setAddress(ConcurrentMap<Lang, String> address);

}
