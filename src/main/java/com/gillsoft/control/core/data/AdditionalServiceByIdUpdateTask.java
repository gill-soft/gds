package com.gillsoft.control.core.data;

import java.io.Serializable;

import com.gillsoft.control.service.MsDataService;

public class AdditionalServiceByIdUpdateTask extends MsDataObjectUpdateTask implements Serializable {

	private static final long serialVersionUID = 90698921706086221L;
	
	private long id;
	
	public AdditionalServiceByIdUpdateTask() {
		
	}

	public AdditionalServiceByIdUpdateTask(long id) {
		this.id = id;
	}

	@Override
	protected String getCacheKey() {
		return MsDataController.getAdditionalServiceCacheKey(id);
	}

	@Override
	protected Object getDataObject(MsDataService service) {
		return service.getAdditionalService(id);
	}

}
