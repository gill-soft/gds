package com.gillsoft.control.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gillsoft.model.Customer;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.Client;

public class ClientView extends Client implements SurnameView, NameView, ContactView, AppUserTokenView, LanguageView {

	private static final long serialVersionUID = -586322446060696668L;
	
	private boolean sendValidationCode;

	public boolean isSendValidationCode() {
		return sendValidationCode;
	}

	public void setSendValidationCode(boolean sendValidationCode) {
		this.sendValidationCode = sendValidationCode;
	}

	@Override
	@JsonIgnore
	public BaseEntity getBaseEntity() {
		return this;
	}
	
	public void setFields(Customer customer) {
		setLogin(customer.getPhone());
		setPhone(customer.getPhone());
		setEmail(customer.getEmail());
		setName(customer.getName());
		setSurname(customer.getSurname());
	}

}
