package com.gillsoft.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The customer of order")
public class Customer {
	
	@ApiModelProperty(value = "Customer id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Customer name", allowEmptyValue = true)
	private String name;
	
	@ApiModelProperty(value = "Customer patronymic", allowEmptyValue = true)
	private String patronymic;
	
	@ApiModelProperty(value = "Customer surname", allowEmptyValue = true)
	private String surname;
	
	@ApiModelProperty(value = "Customer birthday", allowEmptyValue = true)
	private Date birthday;
	
	@ApiModelProperty(value = "Customer phone", allowEmptyValue = true)
	private String phone;
	
	@ApiModelProperty(value = "Customer email", allowEmptyValue = true)
	private String email;

	public Customer() {
		
	}

	public Customer(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPatronymic() {
		return patronymic;
	}

	public void setPatronymic(String patronymic) {
		this.patronymic = patronymic;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
}
