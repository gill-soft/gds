package com.gillsoft.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The service object")
public class ServiceItem implements Serializable {
	
	private static final long serialVersionUID = -978072913114584020L;

	@ApiModelProperty("The id of created service")
	private String id;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
	@ApiModelProperty("The expire datetime of reserved service in format yyyy-MM-dd HH:mm")
	private Date expire;
	
	@ApiModelProperty(value = "The return condition id. Applied when service prepared to return.", allowEmptyValue = true)
	private String returnConditionId;
	
	@ApiModelProperty(value = "The attribute that describes the confirmed current service or not", allowEmptyValue = true)
	private Boolean confirmed;
	
	@ApiModelProperty(value = "Service number", allowEmptyValue = true)
	private String number;
	
	@ApiModelProperty("Service customer")
	private Customer customer;
	
	@ApiModelProperty("Service segment")
	private Segment segment;
	
	@ApiModelProperty(value = "The customer seat provided to current service", allowEmptyValue = true)
	private Seat seat;
	
	@ApiModelProperty("Service price")
	private Price price;
	
	@ApiModelProperty(value = "The map with additional params",
			allowEmptyValue = true, dataType="java.util.Map[java.lang.String, java.lang.String]")
	private Map<String, String> additionals;
	
	@ApiModelProperty(allowEmptyValue = true)
	private RestError error;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getExpire() {
		return expire;
	}

	public void setExpire(Date expire) {
		this.expire = expire;
	}

	public String getReturnConditionId() {
		return returnConditionId;
	}

	public void setReturnConditionId(String returnConditionId) {
		this.returnConditionId = returnConditionId;
	}

	public Boolean getConfirmed() {
		return confirmed;
	}

	public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public Seat getSeat() {
		return seat;
	}

	public void setSeat(Seat seat) {
		this.seat = seat;
	}

	public Price getPrice() {
		return price;
	}

	public void setPrice(Price price) {
		this.price = price;
	}

	public Map<String, String> getAdditionals() {
		return additionals;
	}

	public void setAdditionals(Map<String, String> additionals) {
		this.additionals = additionals;
	}

	public RestError getError() {
		return error;
	}

	public void setError(RestError error) {
		this.error = error;
	}

}
