package com.gillsoft.model.service;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Locality;
import com.gillsoft.model.request.LocalityRequest;

public interface LocalityService {

	public List<Locality> getAll(LocalityRequest request);
	
	public List<Locality> getUsed(LocalityRequest request);
	
	public Map<String, List<String>> getBinding(LocalityRequest request);
	
}
