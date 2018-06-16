package com.gillsoft.abstract_rest_service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.service.LocalityService;

public abstract class AbstractLocalityService implements LocalityService {
	
	@PostMapping(Method.LOCALITY_ALL)
	public abstract List<Locality> getAll(@RequestBody LocalityRequest request);
	
	@PostMapping(Method.LOCALITY_USED)
	public abstract List<Locality> getUsed(@RequestBody LocalityRequest request);
	
	@PostMapping(Method.LOCALITY_BINDING)
	public abstract Map<String, List<String>> getBinding(@RequestBody LocalityRequest request);

}
