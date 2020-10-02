package com.gillsoft.control.core;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;

public class TripByIdUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = -8205906751323267067L;
	
	private long id;
	
	public TripByIdUpdateTask() {
		
	}

	public TripByIdUpdateTask(long id) {
		this.id = id;
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getTripCacheKey(id);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		return service.getTripWithParentsChilds(id);
	}

}
