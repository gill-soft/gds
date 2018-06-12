package com.gillsoft.core.service;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Locality;

public interface LocalityService {

	public List<Locality> getAll();
	
	public List<Locality> getUsed();
	
	public Map<String, List<String>> getBinding();
	
}
