package com.gillsoft.model.request;

import java.io.Serializable;
import java.util.Objects;

import com.gillsoft.model.Resource;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The params for resource connection")
public class ResourceParams implements Serializable {

	private static final long serialVersionUID = 651193866585508046L;
	
	@ApiModelProperty(value = "Request timeout in milliseconds", required = false, allowEmptyValue = true)
	private Resource resource;

	@ApiModelProperty(value = "Host on which the resource is available", required = true)
	private String host;
	
	@ApiModelProperty(value = "Request timeout in milliseconds", required = false, allowEmptyValue = true)
	private Integer requestTimeout;

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null
				|| !(obj instanceof ResourceParams)) {
			return false;
		}
		ResourceParams params = (ResourceParams) obj;
		return Objects.equals(host, params.host)
				&& params.requestTimeout == requestTimeout;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(host, requestTimeout);
	}

}
