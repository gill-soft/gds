package com.gillsoft.model.service;

import java.util.List;

import com.gillsoft.model.Method;
import com.gillsoft.model.Ping;
import com.gillsoft.model.Resource;

public interface ResourceInfoService {

	public Ping ping(String id);

	public Resource getInfo();

	public List<Method> getAvailableMethods();

}
