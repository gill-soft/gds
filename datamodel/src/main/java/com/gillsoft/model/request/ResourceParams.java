package com.gillsoft.model.request;

import java.util.Objects;

public class ResourceParams {

	private String host;
	private int requestTimeout;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getRequestTimeout() {
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
