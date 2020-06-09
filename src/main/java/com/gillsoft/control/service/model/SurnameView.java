package com.gillsoft.control.service.model;

import com.gillsoft.model.Lang;

public interface SurnameView extends IBaseEntity {
	
	public static final String ATTRIBUTE_NAME = "surname";
	
	default public String getSurname() {
		String surname = getAttributeService().getValue(ATTRIBUTE_NAME, getBaseEntity());
		if (surname == null
				|| surname.isEmpty()) {
			for (Lang lang : Lang.values()) {
				surname = getSurname(lang);
				if (surname != null
						&& !surname.isEmpty()) {
					return surname;
				}
			}
		}
		return surname;
	}
	
	default public void setSurname(String surname) {
		getAttributeService().addAttributeValue(ATTRIBUTE_NAME, surname, getBaseEntity());
	}
	
	default public String getSurname(Lang lang) {
		return getAttributeService().getValue(ATTRIBUTE_NAME + "_" + lang, getBaseEntity());
	}
	
	default public void setSurname(Lang lang, String surname) {
		getAttributeService().addAttributeValue(ATTRIBUTE_NAME + "_" + lang, surname, getBaseEntity());
	}

}
