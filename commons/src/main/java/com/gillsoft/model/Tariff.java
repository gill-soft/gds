package com.gillsoft.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The tariff object")
public class Tariff implements Serializable {

	private static final long serialVersionUID = -2964608164425056900L;

	@ApiModelProperty(value = "Tariff id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Tariff code", allowEmptyValue = true)
	private String code;
	
	@ApiModelProperty(value = "Tariff name", allowEmptyValue = true)
	private String name;
	
	@ApiModelProperty(value = "Tariff description", allowEmptyValue = true)
	private String description;
	
	@ApiModelProperty(value = "Tariff value", allowEmptyValue = true)
	private BigDecimal value;
	
	@ApiModelProperty(value = "The count of available seats to this tariff", allowEmptyValue = true)
	private Integer availableCount;
	
	@ApiModelProperty(value = "The list of applied return conditions to this tariff", allowEmptyValue = true)
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

	public Integer getAvailableCount() {
		return availableCount;
	}

	public void setAvailableCount(Integer availableCount) {
		this.availableCount = availableCount;
	}

	public List<ReturnCondition> getReturnConditions() {
		return returnConditions;
	}

	public void setReturnConditions(List<ReturnCondition> returnConditions) {
		this.returnConditions = returnConditions;
	}

}
