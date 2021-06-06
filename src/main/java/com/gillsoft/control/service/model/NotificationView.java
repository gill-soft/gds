package com.gillsoft.control.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.Notification;

public class NotificationView extends Notification implements DescriptionView {

	private static final long serialVersionUID = -7903040328952418174L;

	@Override
	@JsonIgnore
	public BaseEntity getBaseEntity() {
		return this;
	}

}
