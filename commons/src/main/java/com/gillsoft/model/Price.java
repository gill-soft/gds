package com.gillsoft.model;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The price object")
public class Price {

	@ApiModelProperty("The currency of present amounts")
	private Currency currency;
	
	@ApiModelProperty("Price amount")
	private BigDecimal amount;
	
	@ApiModelProperty(value = "Price vat", allowEmptyValue = true)
	private BigDecimal vat;
	
	@ApiModelProperty("Price tariff")
	private Tariff tariff;
	
	@ApiModelProperty(value = "The list of present commissions", allowEmptyValue = true)
	private List<Commission> commissions;

	public Currency getCurrency() {
		return currency;
	}

	public void setCurrency(Currency currency) {
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getVat() {
		return vat;
	}

	public void setVat(BigDecimal vat) {
		this.vat = vat;
	}

	public Tariff getTariff() {
		return tariff;
	}

	public void setTariff(Tariff tariff) {
		this.tariff = tariff;
	}

	public List<Commission> getCommissions() {
		return commissions;
	}

	public void setCommissions(List<Commission> commissions) {
		this.commissions = commissions;
	}

}
