package com.gillsoft.control.core;

import com.gillsoft.model.AbstractJsonModel;

public class IdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 651911356161511195L;
	
	private long resourceId;

	public IdModel() {
		
	}

	public IdModel(long resourceId, String id) {
		this.resourceId = resourceId;
		setId(id);
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public IdModel create(String json) {
		return (IdModel) super.create(json);
	}
	
}
