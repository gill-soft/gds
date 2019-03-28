package com.gillsoft.control.service.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SegmentConnection implements Serializable {

	private static final long serialVersionUID = -7899877440609475460L;

	private long from;
	
	private long to;
	
	private int minConnectionTime;
	
	private int maxConnectionTime;
	
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

}
