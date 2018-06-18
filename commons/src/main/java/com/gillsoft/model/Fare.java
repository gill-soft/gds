package com.gillsoft.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Fare {

	private String id;
	private String code;
	private String name;
	private String description;
	private BigDecimal value;
	private List<ReturnCondition> returnConditions;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public List<ReturnCondition> getReturnConditions() {
		return returnConditions;
	}

	public void setReturnConditions(List<ReturnCondition> returnConditions) {
		this.returnConditions = returnConditions;
	}

}
