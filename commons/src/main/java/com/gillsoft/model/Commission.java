package com.gillsoft.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The commission object")
public class Commission implements Serializable, Name, Description {

	private static final long serialVersionUID = 8422329504720144298L;

	@ApiModelProperty(value = "Commission id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Commission code", allowEmptyValue = true)
	private String code;
	
	@ApiModelProperty(value = "Commission name on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
	private ConcurrentMap<Lang, String> name;
	
	@ApiModelProperty(value = "Commission description on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
	private ConcurrentMap<Lang, String> description;
	
	@ApiModelProperty("Commission value")
	private BigDecimal value;
	
	@ApiModelProperty(value = "Commission vat", allowEmptyValue = true)
	private BigDecimal vat;
	
	@ApiModelProperty("Commission value calc type")
	private CalcType valueCalcType; // тип начисления комиссии
	
	@ApiModelProperty("Commission vat calc type")
	private CalcType vatCalcType; // тип начисления НДС комиссии
	
	@ApiModelProperty("Commission value type")
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
	
    public ConcurrentMap<Lang, String> getName() {
		return name;
	}

	public void setName(ConcurrentMap<Lang, String> name) {
		this.name = name;
	}

	public ConcurrentMap<Lang, String> getDescription() {
		return description;
	}

	public void setDescription(ConcurrentMap<Lang, String> description) {
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
