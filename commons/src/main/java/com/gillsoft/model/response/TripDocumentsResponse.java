package com.gillsoft.model.response;

import java.util.List;

import com.gillsoft.model.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "The response that contains trip's documents.")
public class TripDocumentsResponse extends Response {

	private static final long serialVersionUID = 9172628641106618682L;
	
	@ApiModelProperty("The list that contains documents of current trip.")
	private List<Document> documents;
	
	public TripDocumentsResponse() {
		
	}

	public TripDocumentsResponse(String id, Exception e) {
		setId(id);
		setException(e);
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
