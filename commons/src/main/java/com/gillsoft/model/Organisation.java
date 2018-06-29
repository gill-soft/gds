package com.gillsoft.model;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The organisation object such as carrier, agent, insurance and etc.")
public class Organisation {

	@ApiModelProperty(value = "Organisation id", allowEmptyValue = true)
	private String id;
	
	@ApiModelProperty(value = "Organisation trade mark", allowEmptyValue = true)
	private String tradeMark;
	
	@ApiModelProperty(value = "The list of organisation phones", allowEmptyValue = true)
	private List<String> phones;
	
	@ApiModelProperty(value = "Organisation names on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
    private ConcurrentMap<Lang, String> name;

	@ApiModelProperty(value = "Organisation addresses on a different language", allowEmptyValue = true,
			dataType="java.util.Map[com.gillsoft.model.Lang, java.lang.String]")
    private ConcurrentMap<Lang, String> address;

	public Organisation() {
		
	}

	public Organisation(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTradeMark() {
		return tradeMark;
	}

	public void setTradeMark(String tradeMark) {
		this.tradeMark = tradeMark;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
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

}
