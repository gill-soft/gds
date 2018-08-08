package com.gillsoft.model.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The request which init search process by selected params")
public class TripSearchRequest extends ResourceRequest {
	
	private static final long serialVersionUID = -2642355880338998510L;

	@ApiModelProperty(value = "The from/to pairs of selected localities for search",
			required = true, dataType = "java.util.List[java.lang.String]")
	private List<String[]> localityPairs;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	@ApiModelProperty(value = "The dates of searches in format yyyy-MM-dd. Its will be used for each pair of localities",
			required = true)
	private List<Date> dates;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	@ApiModelProperty(value = "The back dates of searches in format yyyy-MM-dd. Its will be used for each pair of localities",
			required = true)
	private List<Date> backDates;

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
	
	public List<Date> getBackDates() {
		return backDates;
	}

	public void setBackDates(List<Date> backDates) {
		this.backDates = backDates;
	}

	public static TripSearchRequest createRequest(String[] pair, Date date) {
		return createRequest(pair, date, null);
	}
	
	public static TripSearchRequest createRequest(String[] pair, Date date, Date backDate) {
		TripSearchRequest request = new TripSearchRequest();
		List<String[]> pairs = new ArrayList<>(1);
		pairs.add(pair);
		request.setLocalityPairs(pairs);
		request.setDates(Collections.singletonList(date));
		if (backDate != null) {
			request.setBackDates(Collections.singletonList(backDate));
		}
		return request;
	}
	
}
