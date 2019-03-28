package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Objects;

public class Pair implements Serializable {

	private static final long serialVersionUID = 347638174119126864L;
	
	private long from;
	
	private long to;
	
	private long resourceId;
	
	private int addedDays;

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

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public int getAddedDays() {
		return addedDays;
	}

	public void setAddedDays(int addedDays) {
		this.addedDays = addedDays;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Pair)) {
			return false;
		}
		Pair pair = (Pair) obj;
		return pair.getAddedDays() == addedDays
				&& pair.getFrom() == from
				&& pair.getTo() == to
				&& pair.getResourceId() == resourceId;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(from, to, resourceId, addedDays);
	}

}
