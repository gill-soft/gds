package com.gillsoft.control.service.model;

public interface AppUserTokenView extends IBaseEntity {
	
	default public String getAppUserToken() {
		return getAttributeService().getValue("app_user_token", getBaseEntity());
	}
	
	default public void setAppUserToken(String comment) {
		getAttributeService().addAttributeValue("app_user_token", comment, getBaseEntity());
	}

}
