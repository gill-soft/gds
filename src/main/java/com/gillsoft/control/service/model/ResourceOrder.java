package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@Table(name = "resource_orders")
@JsonInclude(Include.NON_NULL)
public class ResourceOrder implements Serializable {

	private static final long serialVersionUID = -770634921856730159L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Lob
	@Column(name = "native_order_id", nullable = true)
	private String resourceNativeOrderId;
	
	@Column(name = "resource_id", nullable = false)
	private long resourceId;
	
	@Column(name = "resource_param_id", nullable = true)
	private long resourceParamId;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="parent", orphanRemoval = true)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	private Set<ResourceService> services;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
	@JsonIgnore
	private Order parent;

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

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public long getResourceParamId() {
		return resourceParamId;
	}

	public void setResourceParamId(long resourceParamId) {
		this.resourceParamId = resourceParamId;
	}

	public Set<ResourceService> getServices() {
		return services;
	}

	public void setServices(Set<ResourceService> services) {
		this.services = services;
	}
	
	public void addResourceService(ResourceService resourceService) {
		if (services == null) {
			services = new HashSet<>();
		}
		resourceService.setParent(this);
		services.add(resourceService);
	}

	public Order getParent() {
		return parent;
	}

	public void setParent(Order parent) {
		this.parent = parent;
	}

}
