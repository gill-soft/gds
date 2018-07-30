package com.gillsoft.model;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The customer of order")
public class Customer implements Serializable {
	
	private static final long serialVersionUID = 3239592381482158470L;

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
	
	@ApiModelProperty(value = "Customer gender", allowEmptyValue = true)
	private Gender gender;
	
	@ApiModelProperty(value = "Customer citizenship", allowEmptyValue = true)
	private Citizenship citizenship;
	
	@ApiModelProperty(value = "Customer identification document type", allowEmptyValue = true)
	private IdentificationDocumentType documentType;
	
	@ApiModelProperty(value = "Customer identification document number", allowEmptyValue = true)
	private String documentNumber;
	
	@ApiModelProperty(value = "Customer identification document series", allowEmptyValue = true)
	private String documentSeries;

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

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public Citizenship getCitizenship() {
		return citizenship;
	}

	public void setCitizenship(Citizenship citizenship) {
		this.citizenship = citizenship;
	}

	public IdentificationDocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(IdentificationDocumentType documentType) {
		this.documentType = documentType;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getDocumentSeries() {
		return documentSeries;
	}

	public void setDocumentSeries(String documentSeries) {
		this.documentSeries = documentSeries;
	}
	
}
