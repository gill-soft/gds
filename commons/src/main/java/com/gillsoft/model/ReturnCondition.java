package com.gillsoft.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The return condition object")
public class ReturnCondition implements Serializable, Title, Description {

	private static final long serialVersionUID = 6327702254544275676L;

	@ApiModelProperty(value = "Condition id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Condition title on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
	private ConcurrentMap<Lang, String> title;
	
	@ApiModelProperty(value = "Condition description on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
	private ConcurrentMap<Lang, String> description;
	
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

	public ConcurrentMap<Lang, String> getTitle() {
		return title;
	}

	public void setTitle(ConcurrentMap<Lang, String> title) {
		this.title = title;
	}

	public ConcurrentMap<Lang, String> getDescription() {
		return description;
	}

	public void setDescription(ConcurrentMap<Lang, String> description) {
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
