package com.gillsoft.control.service.model;

public interface ContactView extends IBaseEntity {
	
	default public String getPhone() {
		return getAttributeService().getValue("phone", getBaseEntity());
	}
	
	default public void setPhone(String phone) {
		getAttributeService().addAttributeValue("phone", phone, getBaseEntity());
	}
	
	default public String getEmail() {
		return getAttributeService().getValue("email", getBaseEntity());
	}
	
	default public void setEmail(String email) {
		getAttributeService().addAttributeValue("email", email, getBaseEntity());
	}

}
