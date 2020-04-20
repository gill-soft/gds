package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SegmentConnection implements Serializable {

	private static final long serialVersionUID = -7899877440609475460L;
	
	private long from;
	
	private long to;
	
	private int minConnectionTime;
	
	private int maxConnectionTime;
	
	private Set<String> fromResources;
	
	private Set<String> toResources;
	
	private Set<String> fromCarriers;
	
	private Set<String> toCarriers;
	
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

	public int getMinConnectionTime() {
		return minConnectionTime;
	}

	public void setMinConnectionTime(int minConnectionTime) {
		this.minConnectionTime = minConnectionTime;
	}

	public int getMaxConnectionTime() {
		return maxConnectionTime;
	}

	public void setMaxConnectionTime(int maxConnectionTime) {
		this.maxConnectionTime = maxConnectionTime;
	}

	public Set<String> getFromResources() {
		return fromResources;
	}

	public void setFromResources(Set<String> fromResources) {
		this.fromResources = fromResources;
	}

	public Set<String> getToResources() {
		return toResources;
	}

	public void setToResources(Set<String> toResources) {
		this.toResources = toResources;
	}

	public Set<String> getFromCarriers() {
		return fromCarriers;
	}

	public void setFromCarriers(Set<String> fromCarriers) {
		this.fromCarriers = fromCarriers;
	}

	public Set<String> getToCarriers() {
		return toCarriers;
	}

	public void setToCarriers(Set<String> toCarriers) {
		this.toCarriers = toCarriers;
	}

}
