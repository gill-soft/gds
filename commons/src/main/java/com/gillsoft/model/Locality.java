package com.gillsoft.model;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "Locality object")
public class Locality implements Cloneable {
	
	@ApiModelProperty(value = "Locality uniq id", allowEmptyValue = false)
	private String id;

	@ApiModelProperty(value = "Locality code", allowEmptyValue = true)
    private String code;

	@ApiModelProperty(value = "Locality timezone", allowEmptyValue = true)
    private String timezone;

	@ApiModelProperty(value = "Locality names on different language",
			allowEmptyValue = false, dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
    private ConcurrentMap<Lang, String> name;

	@ApiModelProperty(value = "Locality addresses on different language",
			allowEmptyValue = false, dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
    private ConcurrentMap<Lang, String> address;

	@ApiModelProperty(value = "Locality latitude", allowEmptyValue = true)
    private BigDecimal latitude;

	@ApiModelProperty(value = "Locality longitude", allowEmptyValue = true)
    private BigDecimal longitude;

	@ApiModelProperty(value = "Parent locality for current locality",
			allowEmptyValue = true, dataType = "com.gillsoft.model.Locality")
    private Locality parent;
    
	@ApiModelProperty(value = "Locality details", allowEmptyValue = true)
    private String details;

	public Locality() {
		
	}

	public Locality(String id) {
		this.id = id;
	}

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
        if (this.name == null) {
            return null;
        }
        String name = this.name.get(lang);
        if (name == null) {
            for (String pointName : this.name.values()) {
                if (pointName != null) {
                    return pointName;
                }
            }
        }
        return name;
    }

    public void setName(Lang lang, String name) {
        if (name == null) {
            return;
        }
        if (this.name == null) {
        	this.name = new ConcurrentHashMap<>();
        }
        this.name.put(lang, name);
    }

    public String getAddress(Lang lang) {
        if (this.address == null) {
            return null;
        }
        String address = this.address.get(lang);
        if (address == null) {
            for (String pointAddress : this.address.values()) {
                if (pointAddress != null) {
                    return pointAddress;
                }
            }
        }
        return address;
    }

    public void setAddress(Lang lang, String address) {
        if (address == null) {
            return;
        }
        if (this.address == null) {
        	this.address = new ConcurrentHashMap<>();
        }
        this.address.put(lang, address);
    }

	public ConcurrentMap<Lang, String> getName() {
		return name;
	}

	public void setName(ConcurrentMap<Lang, String> name) {
		this.name = name;
	}

	public ConcurrentMap<Lang, String> getAddress() {
		return address;
	}

	public void setAddress(ConcurrentMap<Lang, String> address) {
		this.address = address;
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
	public Locality clone() throws CloneNotSupportedException {
		Locality copy = (Locality) super.clone();
		if (name != null) {
			for (Entry<Lang, String> entry : name.entrySet()) {
				copy.setName(entry.getKey(), entry.getValue());
			}
		}
		if (address != null) {
			for (Entry<Lang, String> entry : address.entrySet()) {
				copy.setAddress(entry.getKey(), entry.getValue());
			}
		}
		if (latitude != null) {
			copy.latitude = new BigDecimal(latitude.toString());
		}
		if (longitude != null) {
			copy.longitude = new BigDecimal(longitude.toString());
		}
		if (parent != null) {
			copy.parent = parent.clone();
		}
		return copy;
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
				&& Objects.equals(name, locality.name)
				&& Objects.equals(address, locality.address)
				&& Objects.equals(parent, locality.parent);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, code, details, timezone, latitude, longitude, name, address, parent);
	}
    
}
