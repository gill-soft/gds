package com.gillsoft.control.service;

import com.gillsoft.control.service.model.ConnectionParams;
import com.gillsoft.control.service.model.ConnectionsResponse;

public interface SegmentsConnectionService {
	
	public ConnectionsResponse getConnections(ConnectionParams params);

}
