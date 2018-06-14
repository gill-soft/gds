package com.gillsoft.model;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentMap;

public class Locality {
	
	private String id;

    private String code;

    private String timezone;

    private ConcurrentMap<String, String> names;

    private ConcurrentMap<String, String> addresses;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Locality parent;
    
    private String details;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public ConcurrentMap<String, String> getNames() {
		return names;
	}

	public void setNames(ConcurrentMap<String, String> names) {
		this.names = names;
	}

	public ConcurrentMap<String, String> getAddresses() {
		return addresses;
	}

	public void setAddresses(ConcurrentMap<String, String> addresses) {
		this.addresses = addresses;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public Locality getParent() {
		return parent;
	}

	public void setParent(Locality parent) {
		this.parent = parent;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
    
}
