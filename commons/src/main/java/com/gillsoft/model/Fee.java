package com.gillsoft.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Fee {

	private String id;
	private String code;
	private String name;
	private String description;
	private BigDecimal value;
	private BigDecimal vat;
	private CalcType valueCalcType; // тип начисления комиссии
	private CalcType vatCalcType; // тип начисления НДС комиссии
	private ValueType type; // тип измерения значения комиссии (расчитанное значение или процент)

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

	public BigDecimal getVat() {
		return vat;
	}

	public void setVat(BigDecimal vat) {
		this.vat = vat;
	}

	public CalcType getValueCalcType() {
		return valueCalcType;
	}

	public void setValueCalcType(CalcType valueCalcType) {
		this.valueCalcType = valueCalcType;
	}

	public CalcType getVatCalcType() {
		return vatCalcType;
	}

	public void setVatCalcType(CalcType vatCalcType) {
		this.vatCalcType = vatCalcType;
	}

	public ValueType getType() {
		return type;
	}

	public void setType(ValueType type) {
		this.type = type;
	}

}
