package com.gillsoft.control.service.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "groupe_ids")
public class GroupeIdEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "groupe_id")
	private long groupeId;

	public GroupeIdEntity() {
		
	}

	public GroupeIdEntity(long id, long groupeId) {
		this.id = id;
		this.groupeId = groupeId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getGroupeId() {
		return groupeId;
	}

	public void setGroupeId(long groupeId) {
		this.groupeId = groupeId;
	}

}
