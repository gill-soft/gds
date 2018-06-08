package com.gillsoft.service;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Location;

public interface LocationService {

	public List<Location> getAll();
	
	public List<Location> getUsed();
	
	public Map<String, List<String>> getBinding();
	
}
