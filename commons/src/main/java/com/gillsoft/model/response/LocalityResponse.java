package com.gillsoft.model.response;

import java.util.List;
import java.util.Map;

import com.gillsoft.model.Locality;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response with localities containers")
public class LocalityResponse extends Response {
	
	@ApiModelProperty(value = "The list of localities", allowEmptyValue = true)
	private List<Locality> localities;
	
	@ApiModelProperty(value = "The map with from/to bindings. Contains only locality's ids.",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, java.util.List[java.lang.String]]")
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
