package com.gillsoft.concurrent;

public enum PoolType {
	
	RESOURCE_INFO(10),
	LOCALITY(20),
	SEARCH(100);

	private int size;
	
	private PoolType(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
	
}
