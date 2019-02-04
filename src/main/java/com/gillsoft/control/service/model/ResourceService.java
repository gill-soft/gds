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
@Table(name = "resource_services")
@JsonInclude(Include.NON_NULL)
public class ResourceService implements Serializable {

	private static final long serialVersionUID = -457183879332671903L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Lob
	@Column(name = "native_service_id", nullable = true)
	private String resourceNativeServiceId;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", orphanRemoval = true)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	private Set<ServiceStatusEntity> statuses;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_order_id", nullable = false)
	@JsonIgnore
	private ResourceOrder parent;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getResourceNativeServiceId() {
		return resourceNativeServiceId;
	}

	public void setResourceNativeServiceId(String resourceNativeServiceId) {
		this.resourceNativeServiceId = resourceNativeServiceId;
	}

	public Set<ServiceStatusEntity> getStatuses() {
		return statuses;
	}

	public void setStatuses(Set<ServiceStatusEntity> statuses) {
		this.statuses = statuses;
	}
	
	public void addStatus(ServiceStatusEntity status) {
		if (statuses == null) {
			statuses = new HashSet<>();
		}
		status.setParent(this);
		statuses.add(status);
	}

	public ResourceOrder getParent() {
		return parent;
	}

	public void setParent(ResourceOrder parent) {
		this.parent = parent;
	}

}
