package com.gillsoft.control.service.model;

import com.gillsoft.ms.entity.Resource;

public class AdditionalServiceEmptyResource extends Resource {

	private static final long serialVersionUID = -8438825614309396447L;
	
	private static final long ID = -1000;

	public AdditionalServiceEmptyResource() {
		setId(ID);
	}
	
	public static boolean isThisId(String id) {
		return String.valueOf(ID).equals(id);
	}
	
	public static boolean isThisId(long id) {
		return id == ID;
	}
	
}
