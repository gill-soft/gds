package com.gillsoft.model.request;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The request which init search process by selected params")
public class TripSearchRequest extends ResourceRequest {
	
	@ApiModelProperty(value = "The from/to pairs of selected localities for search",
			required = true, dataType = "java.util.List[java.lang.String]")
	private List<String[]> localityPairs;
	
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd")
	@ApiModelProperty(value = "The of searches dates in format yyyy-MM-dd. Its will be used for each pair of localities",
			required = true)
	private List<Date> dates;

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
