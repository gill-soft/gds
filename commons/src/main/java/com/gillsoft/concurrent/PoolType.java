package com.gillsoft.concurrent;

public enum PoolType implements BasePoolType {
	
	RESOURCE_INFO(10),
	LOCALITY(20),
	SEARCH(100),
	ORDER(20);

	private int size;
	
	private PoolType(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}
	
}
