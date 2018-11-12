package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.ReturnCondition;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "The response that contains trip's return conditions for selected tariff.")
public class ReturnConditionResponse extends Response {

	private static final long serialVersionUID = 264067700891232710L;
	
	private List<ReturnCondition> conditions;
	
	public ReturnConditionResponse() {
		
	}

	public ReturnConditionResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public List<ReturnCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<ReturnCondition> conditions) {
		this.conditions = conditions;
	}

}
