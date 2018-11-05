package com.gillsoft.model.response;

import com.gillsoft.model.SeatsScheme;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The response which contains the scheme of seats in the transport")
public class SeatsSchemeResponse extends Response {

	private static final long serialVersionUID = -3436890654152101256L;
	
	@ApiModelProperty("The seats scheme of selected trip")
	private SeatsScheme scheme;
	
	public SeatsSchemeResponse() {
		
	}

	public SeatsSchemeResponse(String id, SeatsScheme scheme) {
		setId(id);
		this.scheme = scheme;
	}
	
	public SeatsSchemeResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public SeatsScheme getScheme() {
		return scheme;
	}

	public void setScheme(SeatsScheme scheme) {
		this.scheme = scheme;
	}

}
