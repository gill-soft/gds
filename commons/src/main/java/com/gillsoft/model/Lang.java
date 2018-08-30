package com.gillsoft.model;

import java.util.concurrent.ConcurrentMap;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The dictionary of used languages")
public enum Lang {
	
	UA,
	RU,
	PL,
	EN;
	
	static String getValue(ConcurrentMap<Lang, String> valueMap, Lang lang) {
		if (valueMap == null) {
            return null;
        }
        String name = valueMap.get(lang);
        if (name == null) {
            for (String value : valueMap.values()) {
                if (value != null) {
                    return value;
                }
            }
        }
        return name;
	}

}
