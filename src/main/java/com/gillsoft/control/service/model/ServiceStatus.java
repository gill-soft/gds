package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.gillsoft.model.Price;

@Entity
@Table(name = "service_statuses")
public class ServiceStatus implements Serializable {

	private static final long serialVersionUID = 7197097579192677781L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false)
	private Date created;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@Column(name = "user_id", nullable = false)
	private long userId;
	
	@Column(name = "org_id", nullable = false)
	private long organisationId;
	
	@Lob
	@Column(nullable = true)
	private String error;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_service_id", nullable = false)
	private ResourceService parent;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "status", orphanRemoval = true)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	private StatusPrice price;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getOrganisationId() {
		return organisationId;
	}

	public void setOrganisationId(long organisationId) {
		this.organisationId = organisationId;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public ResourceService getParent() {
		return parent;
	}

	public void setParent(ResourceService parent) {
		this.parent = parent;
	}

	public StatusPrice getPrice() {
		return price;
	}

	public void setPrice(StatusPrice price) {
		this.price = price;
	}
	
	public void setPrice(Price price) {
		if (price != null) {
			StatusPrice statusPrice = new StatusPrice();
			statusPrice.setPrice(price);
			statusPrice.setStatus(this);
		}
	}

}
