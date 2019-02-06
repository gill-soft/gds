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
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.Price;
import com.gillsoft.model.ServiceStatus;

@Entity
@Table(name = "service_statuses")
@JsonInclude(Include.NON_NULL)
public class ServiceStatusEntity implements Serializable {

	private static final long serialVersionUID = 7197097579192677781L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false)
	private Date created;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ServiceStatus status;
	
	@Column(name = "user_id", nullable = false)
	private long userId;
	
	@Column(name = "org_id", nullable = false)
	private long organisationId;
	
	@Lob
	@Column(nullable = true)
	private String error;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_service_id", nullable = false)
	@JsonIgnore
	private ResourceService parent;
	
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "status", orphanRemoval = true)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	private StatusPrice price;
	
	@Transient
	private ServiceStatus prevStatus;
	
	private boolean reported;

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

	public ServiceStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceStatus status) {
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
			this.price = statusPrice;
		}
	}

	public boolean isReported() {
		return reported;
	}

	public void setReported(boolean reported) {
		this.reported = reported;
	}

	public ServiceStatus getPrevStatus() {
		return prevStatus;
	}

	public void setPrevStatus(ServiceStatus prevStatus) {
		this.prevStatus = prevStatus;
	}

	@PreUpdate
	@PrePersist
	@PreRemove
	public void prepareUpdate() {
		if (prevStatus != null) {
			status = prevStatus;
		}
	}

}
