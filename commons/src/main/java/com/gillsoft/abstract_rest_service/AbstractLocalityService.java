package com.gillsoft.abstract_rest_service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.service.LocalityService;

public abstract class AbstractLocalityService implements LocalityService {
	
	@Override
	@PostMapping(Method.LOCALITY_ALL)
	public final List<Locality> getAll(@RequestBody LocalityRequest request) {
		return getAllResponse(request);
	}
	
	public abstract List<Locality> getAllResponse(LocalityRequest request);
	
	@Override
	@PostMapping(Method.LOCALITY_USED)
	public final List<Locality> getUsed(@RequestBody LocalityRequest request) {
		return getUsedResponse(request);
	}
	
	public abstract List<Locality> getUsedResponse(LocalityRequest request);
	
	@Override
	@PostMapping(Method.LOCALITY_BINDING)
	public final Map<String, List<String>> getBinding(@RequestBody LocalityRequest request) {
		return getBindingResponse(request);
	}
	
	public abstract Map<String, List<String>> getBindingResponse(LocalityRequest request);
	
	public static Locality getLocality(List<Locality> localities, String id) {
		if (localities == null) {
			return null;
		}
		for (Locality locality : localities) {
			if (Objects.equals(id, locality.getId())) {
				return locality;
			}
		}
		return null;
	}

}
