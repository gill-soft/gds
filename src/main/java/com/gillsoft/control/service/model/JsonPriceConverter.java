package com.gillsoft.control.service.model;

import java.io.IOException;

import javax.persistence.AttributeConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gillsoft.model.Price;
import com.gillsoft.util.StringUtil;

public class JsonPriceConverter implements AttributeConverter<Price, String> {

	@Override
	public String convertToDatabaseColumn(Price price) {
		if (price == null) {
			return null;
		}
		try {
			return StringUtil.objectToJsonString(price);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public Price convertToEntityAttribute(String value) {
		if (value == null) {
			return null;
		}
		try {
			return StringUtil.jsonStringToObject(Price.class, value);
		} catch (IOException e) {
			return null;
		}
	}

}
