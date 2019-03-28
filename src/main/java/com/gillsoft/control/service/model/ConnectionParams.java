package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ConnectionParams implements Serializable {

	private static final long serialVersionUID = 595553492958781800L;

	private long from;

	private long to;

	private Integer maxConnections;

	private Integer minConnectionTime;

	private Integer maxConnectionTime;

	private Set<Long> resources;

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public Integer getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(Integer maxConnections) {
		this.maxConnections = maxConnections;
	}

	public Integer getMinConnectionTime() {
		return minConnectionTime;
	}

	public void setMinConnectionTime(Integer minConnectionTime) {
		this.minConnectionTime = minConnectionTime;
	}

	public Integer getMaxConnectionTime() {
		return maxConnectionTime;
	}

	public void setMaxConnectionTime(Integer maxConnectionTime) {
		this.maxConnectionTime = maxConnectionTime;
	}

	public Set<Long> getResources() {
		return resources;
	}

	public void setResources(Set<Long> resources) {
		this.resources = resources;
	}

}
