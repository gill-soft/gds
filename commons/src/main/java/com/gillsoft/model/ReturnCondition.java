package com.gillsoft.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The return condition object")
public class ReturnCondition implements Serializable {

	private static final long serialVersionUID = 6327702254544275676L;

	@ApiModelProperty(value = "Condition id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Condition title", allowEmptyValue = true)
	private String title;
	
	@ApiModelProperty(value = "Condition description", allowEmptyValue = true)
	private String description;
	
	@ApiModelProperty(value = "The minutes before depart when condition is available", allowEmptyValue = true)
	private Integer minutesBeforeDepart;
	
	@ApiModelProperty(value = "The returned percent from tariff value", allowEmptyValue = true)
	private BigDecimal returnPercent;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getMinutesBeforeDepart() {
		return minutesBeforeDepart;
	}

	public void setMinutesBeforeDepart(Integer minutesBeforeDepart) {
		this.minutesBeforeDepart = minutesBeforeDepart;
	}

	public BigDecimal getReturnPercent() {
		return returnPercent;
	}

	public void setReturnPercent(BigDecimal returnPercent) {
		this.returnPercent = returnPercent;
	}

}
