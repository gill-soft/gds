package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.Tariff;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The response which contains the list of trips tariffs")
public class TariffsResponse extends Response {

	private static final long serialVersionUID = -2191474374995541021L;

	@ApiModelProperty(value = "The list of trips tariffs", allowEmptyValue = true)
	private List<Tariff> tariffs;
	
	public TariffsResponse() {
		
	}

	public TariffsResponse(String id, List<Tariff> tariffs) {
		setId(id);
		this.tariffs = tariffs;
	}
	
	public TariffsResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public List<Tariff> getTariffs() {
		return tariffs;
	}

	public void setTariffs(List<Tariff> tariffs) {
		this.tariffs = tariffs;
	}
	
}
