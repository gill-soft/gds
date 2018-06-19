package com.gillsoft.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Required {
	
	private Map<String, Boolean> fields;

	public Map<String, Boolean> getFields() {
		return fields;
	}

	public void setFields(Map<String, Boolean> fields) {
		this.fields = fields;
	}
	
}
