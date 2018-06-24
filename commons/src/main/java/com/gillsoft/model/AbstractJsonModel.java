package com.gillsoft.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.util.StringUtil;

public abstract class AbstractJsonModel extends ResourceRequest {
	
	public String asString() {
		try {
			return StringUtil.objectToJsonBase64String(this);
		} catch (JsonProcessingException e) {
			return "";		}
	}
	
	public AbstractJsonModel create(String json) {
		try {
			return StringUtil.jsonBase64StringToObject(getClass(), json);
		} catch (IOException e) {
			return null;
		}
	}

}
