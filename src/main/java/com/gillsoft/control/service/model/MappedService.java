package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.gillsoft.model.ApiDateDeserializer;
import com.gillsoft.model.ApiDateSerializer;
import com.gillsoft.model.ApiDateTimeDeserializer;
import com.gillsoft.model.ApiDateTimeSerializer;

@Entity
@Table(name = "mapped_services", indexes = { @Index(columnList = "carrier_id,trip_departure"),
		@Index(columnList = "trip_id,trip_departure"),
		@Index(columnList = "trip_id,from_id,from_departure"),
		@Index(columnList = "trip_id,to_id,to_departure") },
		uniqueConstraints = { @UniqueConstraint(columnNames = { "trip_id", "from_id", "to_id", "trip_departure", "from_departure", "to_departure", "resource_service_id" }) })
@JsonInclude(Include.NON_NULL)
public class MappedService implements Serializable {

	private static final long serialVersionUID = 3851796582983927265L;

	public static final String MAPPED_SERVICES_KEY = "MAPPED_SERVICES_KEY";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "order_index")
	private int order;
	
	@Column(name = "carrier_id")
	private long carrierId;

	@Column(name = "trip_id")
	private long tripId;

	@Column(name = "from_id")
	private long fromId;

	@Column(name = "to_id")
	private long toId;

	@Column(name = "trip_departure")
	@Temporal(TemporalType.DATE)
	@JsonSerialize(using = ApiDateSerializer.class)
	@JsonDeserialize(using = ApiDateDeserializer.class)
	private Date tripDeparture;

	@Column(name = "from_departure")
	@JsonSerialize(using = ApiDateTimeSerializer.class)
	@JsonDeserialize(using = ApiDateTimeDeserializer.class)
	private Date fromDeparture;
	
	@Column(name = "to_departure")
	@JsonSerialize(using = ApiDateTimeSerializer.class)
	@JsonDeserialize(using = ApiDateTimeDeserializer.class)
	private Date toDeparture;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "resource_service_id", nullable = false)
	@JsonIgnore
	private ResourceService parent;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public long getCarrierId() {
		return carrierId;
	}

	public void setCarrierId(long carrierId) {
		this.carrierId = carrierId;
	}

	public long getTripId() {
		return tripId;
	}

	public void setTripId(long tripId) {
		this.tripId = tripId;
	}

	public long getFromId() {
		return fromId;
	}

	public void setFromId(long fromId) {
		this.fromId = fromId;
	}

	public long getToId() {
		return toId;
	}

	public void setToId(long toId) {
		this.toId = toId;
	}

	public Date getTripDeparture() {
		return tripDeparture;
	}

	public void setTripDeparture(Date tripDeparture) {
		this.tripDeparture = tripDeparture;
	}

	public Date getFromDeparture() {
		return fromDeparture;
	}

	public void setFromDeparture(Date fromDeparture) {
		this.fromDeparture = fromDeparture;
	}

	public Date getToDeparture() {
		return toDeparture;
	}

	public void setToDeparture(Date toDeparture) {
		this.toDeparture = toDeparture;
	}

	public ResourceService getParent() {
		return parent;
	}

	public void setParent(ResourceService parent) {
		this.parent = parent;
	}

}
