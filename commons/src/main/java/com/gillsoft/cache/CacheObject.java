package com.gillsoft.cache;

import java.io.Serializable;

public class CacheObject implements Serializable {

	private static final long serialVersionUID = -6596207226068125817L;
	
	private String name;
	private Object cachedObject;
	private boolean readed = false;
	private boolean eternal = false;
	private long created = System.currentTimeMillis();
	private long timeToLive;
	private Runnable updateTask;
	private long updateDelay;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getCachedObject() {
		return cachedObject;
	}
	
	public void setCachedObject(Object cachedObject) {
		this.cachedObject = cachedObject;
	}
	
	public boolean isReaded() {
		return readed;
	}

	public void setReaded(boolean readed) {
		this.readed = readed;
	}

	public boolean isEternal() {
		return eternal;
	}

	public void setEternal(boolean eternal) {
		this.eternal = eternal;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = created + timeToLive;
	}

	public Runnable getUpdateTask() {
		return updateTask;
	}

	public void setUpdateTask(Runnable updateTask) {
		this.updateTask = updateTask;
	}

	public long getUpdateDelay() {
		return updateDelay;
	}

	public void setUpdateDelay(long updateDelay) {
		this.updateDelay = updateDelay;
	}
	
	public int getRemainingTime() {
		return (int) ((timeToLive - System.currentTimeMillis()) / 1000);
	}
	
}
