package com.gillsoft.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
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

	public String getName(String lang) {
        if (names == null) {
            return null;
        }
        String name = names.get(lang);
        if (name == null) {
            for (String pointName : names.values()) {
                if (pointName != null) {
                    return pointName;
                }
            }
        }
        return name;
    }

    public void setName(String lang, String name) {
        if (name == null) {
            return;
        }
        if (names == null) {
            names = new ConcurrentHashMap<>();
        }
        names.put(lang, name);
    }

    public String getAddress(String lang) {
        if (addresses == null) {
            return null;
        }
        String address = addresses.get(lang);
        if (address == null) {
            for (String pointAddress : addresses.values()) {
                if (pointAddress != null) {
                    return pointAddress;
                }
            }
        }
        return address;
    }

    public void setAddress(String lang, String address) {
        if (address == null) {
            return;
        }
        if (addresses == null) {
            addresses = new ConcurrentHashMap<>();
        }
        addresses.put(lang, address);
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
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null
				|| !(obj instanceof Locality)) {
			return false;
		}
		Locality locality = (Locality) obj;
		return Objects.equals(id, locality.id)
				&& Objects.equals(code, locality.code)
				&& Objects.equals(details, locality.details)
				&& Objects.equals(timezone, locality.timezone)
				&& Objects.equals(latitude, locality.latitude)
				&& Objects.equals(longitude, locality.longitude)
				&& Objects.equals(names, locality.names)
				&& Objects.equals(addresses, locality.addresses)
				&& Objects.equals(parent, locality.parent);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, code, details, timezone, latitude, longitude, names, addresses, parent);
	}
    
}
