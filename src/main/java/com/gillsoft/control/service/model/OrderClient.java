package com.gillsoft.control.service.model;

import java.util.Objects;
import java.util.Optional;

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
import javax.persistence.UniqueConstraint;

import com.gillsoft.ms.entity.AttributeValue;
import com.gillsoft.ms.entity.User;

@Entity
@Table(name = "clients", indexes = { @Index(columnList = "client_id"), @Index(columnList = "phone") },
		uniqueConstraints = { @UniqueConstraint(columnNames = { "client_id", "phone", "order_id" }) })
public class OrderClient {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@Column(name = "client_id", nullable = true)
	private long clientId;
	
	@Column(nullable = true)
	private String phone;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
	private Order parent;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = preparePhone(phone);
	}

	public Order getParent() {
		return parent;
	}

	public void setParent(Order parent) {
		this.parent = parent;
	}
	
	public void setFields(User client) {
		setClientId(client.getId());
		setPhone(getPhone(client));
	}
	
	public static String getPhone(User client) {
		if (client.getAttributeValues() != null) {
			Optional<AttributeValue> attribute = client.getAttributeValues().stream().filter(av -> "phone".equals(av.getAttribute().getName())).findFirst();
			return attribute.isPresent() ? attribute.get().getValue() : null;
		}
		return null;
	}
	
	public static String preparePhone(String phone) {
		if (phone  != null) {
			phone = phone.replaceAll("\\D", "");
		}
		return phone;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null
				&& obj instanceof OrderClient) {
			OrderClient client = (OrderClient) obj;
			return client.getClientId() == clientId
					&& Objects.equals(client.getPhone(), phone)
					&& isParentsEquals(client);
		}
		return false;
	}
	
	private boolean isParentsEquals(OrderClient client) {
		if (client.getParent() == null
				&& parent == null) {
			return true;
		}
		if (client.getParent() != null
				&& parent != null) {
			return client.getParent().getId() == parent.getId();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(clientId, phone, parent != null ? parent.getId() : null);
	}
	
}
