package com.gillsoft.control.core;

import com.gillsoft.model.AbstractJsonModel;

public class IdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 651911356161511195L;
	
	private long resourceId;
	
	private String id;

	public IdModel() {
		
	}

	public IdModel(long resourceId, String id) {
		this.resourceId = resourceId;
		this.id = id;
	}

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public IdModel create(String json) {
		return (IdModel) super.create(json);
	}
	
}
