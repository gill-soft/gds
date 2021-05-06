package com.gillsoft.control.service;

import java.util.List;

import com.gillsoft.model.request.AdditionalDetailsRequest;
import com.gillsoft.model.request.AdditionalSearchRequest;
import com.gillsoft.model.response.AdditionalSearchResponse;
import com.gillsoft.model.response.DocumentsResponse;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.TariffsResponse;

public interface AgregatorAdditionalSearchService {
	
	public AdditionalSearchResponse initSearch(List<AdditionalSearchRequest> request);
	
	public AdditionalSearchResponse getSearchResult(String searchId);
	
	public List<TariffsResponse> getTariffs(List<AdditionalDetailsRequest> requests);
	
	public List<RequiredResponse> getRequiredFields(List<AdditionalDetailsRequest> requests);
	
	public List<ReturnConditionResponse> getConditions(List<AdditionalDetailsRequest> requests);
	
	public List<DocumentsResponse> getDocuments(List<AdditionalDetailsRequest> requests);

}
