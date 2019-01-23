package com.gillsoft.control.service.model;

import java.io.IOException;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.util.StringUtil;

public class JsonResponseConverter implements AttributeConverter<OrderResponse, String> {

	@Override
	public String convertToDatabaseColumn(OrderResponse response) {
		if (response == null) {
			return null;
		}
		try {
			return StringUtil.objectToJsonString(response);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public OrderResponse convertToEntityAttribute(String value) {
		if (value == null) {
			return null;
		}
		try {
			return StringUtil.jsonStringToObject(OrderResponse.class, value);
		} catch (IOException e) {
			return null;
		}
	}

}
