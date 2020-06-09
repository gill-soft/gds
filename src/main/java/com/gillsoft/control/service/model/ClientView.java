package com.gillsoft.control.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gillsoft.model.Customer;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.Client;

public class ClientView extends Client implements SurnameView, NameView, ContactView {

	private static final long serialVersionUID = 5327744450784387112L;

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
