package com.gillsoft.model.response;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Locality;

public class LocalityResponse extends Response {
	
	private List<Locality> localities;
	
	private Map<String, List<String>> binding;
	
	public LocalityResponse(String id, List<Locality> localities) {
		setId(id);
		this.localities = localities;
	}
	
	public LocalityResponse(String id, Map<String, List<String>> binding) {
		setId(id);
		this.binding = binding;
	}
	
	public LocalityResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public List<Locality> getLocalities() {
		return localities;
	}

	public void setLocalities(List<Locality> localities) {
		this.localities = localities;
	}

	public Map<String, List<String>> getBinding() {
		return binding;
	}

	public void setBinding(Map<String, List<String>> binding) {
		this.binding = binding;
	}
	
}
