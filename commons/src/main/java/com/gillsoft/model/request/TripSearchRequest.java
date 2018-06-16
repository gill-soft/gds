package com.gillsoft.model.request;

import java.util.Date;
import java.util.List;

public class TripSearchRequest extends ResourceRequest {
	
	private String searchId;
	private List<String[]> localityPairs;
	private List<Date> dates;

	public String getSearchId() {
		return searchId;
	}

	public void setSearchId(String searchId) {
		this.searchId = searchId;
	}

	public List<String[]> getLocalityPairs() {
		return localityPairs;
	}

	public void setLocalityPairs(List<String[]> localityPairs) {
		this.localityPairs = localityPairs;
	}

	public List<Date> getDates() {
		return dates;
	}

	public void setDates(List<Date> dates) {
		this.dates = dates;
	}
	
}
