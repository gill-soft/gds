package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "resource_orders")
public class ResourceOrder implements Serializable {

	private static final long serialVersionUID = -770634921856730159L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Lob
	@Column(name = "native_order_id", nullable = false)
	private String resourceNativeOrderId;
	
	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "resource_order_id")
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	private Set<ResourceService> services;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getResourceNativeOrderId() {
		return resourceNativeOrderId;
	}

	public void setResourceNativeOrderId(String resourceNativeOrderId) {
		this.resourceNativeOrderId = resourceNativeOrderId;
	}

	public Set<ResourceService> getServices() {
		return services;
	}

	public void setServices(Set<ResourceService> services) {
		this.services = services;
	}

}
