package com.gillsoft.control.service.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.gillsoft.model.PaymentMethod;
import com.gillsoft.model.response.OrderResponse;

@Entity
@Table(name = "orders")
@JsonInclude(Include.NON_NULL)
public class Order implements Serializable {

	private static final long serialVersionUID = -3584245107941218642L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(nullable = false)
	private Date created;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private PaymentMethod payment = PaymentMethod.NON_CASH;
	
	@Column(name="cancel_reason", nullable = true)
	private String cancelReason;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", orphanRemoval = true)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	private Set<ResourceOrder> orders;
	
	@Column(nullable = false, columnDefinition = "json")
	@Convert(converter = JsonResponseConverter.class)
	private OrderResponse response;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", orphanRemoval = true)
	@Cascade({ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.SAVE_UPDATE })
	@Fetch(FetchMode.SELECT)
	@JsonIgnore
	private Set<OrderDocument> documents;

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

	public PaymentMethod getPayment() {
		return payment;
	}

	public void setPayment(PaymentMethod payment) {
		this.payment = payment;
	}

	public String getCancelReason() {
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	public Set<ResourceOrder> getOrders() {
		return orders;
	}

	public void setOrders(Set<ResourceOrder> orders) {
		this.orders = orders;
	}
	
	public void addResourceOrder(ResourceOrder resourceOrder) {
		if (orders == null) {
			orders = new HashSet<>();
		}
		resourceOrder.setParent(this);
		orders.add(resourceOrder);
	}

	public OrderResponse getResponse() {
		return response;
	}

	public void setResponse(OrderResponse response) {
		this.response = response;
	}

	public Set<OrderDocument> getDocuments() {
		return documents;
	}

	public void setDocuments(Set<OrderDocument> documents) {
		this.documents = documents;
	}
	
	public void addOrderDocument(OrderDocument orderDocument) {
		if (documents == null) {
			documents = new HashSet<>();
		}
		orderDocument.setParent(this);
		documents.add(orderDocument);
	}

}
