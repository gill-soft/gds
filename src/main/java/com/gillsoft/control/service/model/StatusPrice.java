package com.gillsoft.control.service.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gillsoft.model.Price;

@Entity
@Table(name = "status_price")
public class StatusPrice implements Serializable {
	
	private static final long serialVersionUID = -4558394180946607682L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false, columnDefinition = "json")
	@Convert(converter = JsonPriceConverter.class)
	private Price price;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
	@JsonIgnore
	private ServiceStatusEntity status;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Price getPrice() {
		return price;
	}

	public void setPrice(Price price) {
		this.price = price;
	}

	public ServiceStatusEntity getStatus() {
		return status;
	}

	public void setStatus(ServiceStatusEntity status) {
		this.status = status;
	}

}
