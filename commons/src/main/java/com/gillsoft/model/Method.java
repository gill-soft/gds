package com.gillsoft.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Method {

	public static final String PING = "/api/ping";
	
	public static final String INFO = "/api/info";
	
	public static final String METHOD = "/api/method";
	
	public static final String LOCALITY_ALL = "/api/locality/all";
	
	public static final String LOCALITY_USED = "/api/locality/used";
	
	public static final String LOCALITY_BINDING = "/api/locality/binding";
	
	public static final String SEARCH = "/api/search";
	
	public static final String SEARCH_TRIP_ROUTE = "/api/search/trip/route";
	
	public static final String SEARCH_TRIP_SEATS_SCHEME = "/api/search/trip/seats/scheme";
	
	public static final String SEARCH_TRIP_SEATS = "/api/search/trip/seats";
	
	public static final String SEARCH_TRIP_TARIFFS = "/api/search/trip/tariffs";
	
	public static final String SEARCH_TRIP_REQUIRED = "/api/search/trip/required";
	
	public static final String SEARCH_TRIP_CONDITIONS = "/api/search/trip/conditions";
	
	public static final String SEARCH_TRIP_DOCUMENTS = "/api/search/trip/documents";
	
	public static final String ORDER = "/api/order";
	
	public static final String ORDER_BOOKING = "/api/order/booking";
	
	public static final String ORDER_CONFIRM = "/api/order/confirm";
	
	public static final String ORDER_CANCEL = "/api/order/cancel";
	
	public static final String ORDER_SERVICE = "/api/order/service";
	
	public static final String ORDER_SERVICE_ADD = "/api/order/service/add";
	
	public static final String ORDER_SERVICE_REMOVE = "/api/order/service/remove";
	
	public static final String ORDER_CUSTOMER_UPDATE = "/api/order/customer/update";
	
	public static final String ORDER_RETURN_PREPARE = "/api/order/return/prepare";
	
	public static final String ORDER_RETURN_CONFIRM = "/api/order/return/confirm";
	
	public static final String ORDER_DOCUMENTS = "/api/order/document";
	
	private String name;
	
	private String url;
	
	private MethodType type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MethodType getType() {
		return type;
	}

	public void setType(MethodType type) {
		this.type = type;
	}
	
}
