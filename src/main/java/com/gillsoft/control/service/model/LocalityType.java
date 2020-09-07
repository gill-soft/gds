package com.gillsoft.control.service.model;

public enum LocalityType {
	
	COUNTRY,
	LOCALITY,
	STOPPING,
	OTHER;
	
	public static LocalityType get(String type) {
		if (type == null) {
			return OTHER;
		}
		switch (type) {
		case "6":
			return LOCALITY;
		case "1":
			return COUNTRY;
		case "2":
		case "3":
		case "4":
		case "5":
			return OTHER;
		default:
			return OTHER;
		}
	}

}
