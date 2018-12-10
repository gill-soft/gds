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
@Table(name = "resource_services")
public class ResourceService implements Serializable {

	private static final long serialVersionUID = -457183879332671903L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Lob
	@Column(name = "native_service_id", nullable = false)
	private String resourceNativeServiceId;
	
	@Column(name = "resource_id", nullable = false)
	private long resourceId;
	
	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true)
	@JoinColumn(name = "resource_service_id")
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	private Set<ServiceStatus> statuses;

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

	public long getResourceId() {
		return resourceId;
	}

	public void setResourceId(long resourceId) {
		this.resourceId = resourceId;
	}

	public Set<ServiceStatus> getStatuses() {
		return statuses;
	}

	public void setStatuses(Set<ServiceStatus> statuses) {
		this.statuses = statuses;
	}

}
