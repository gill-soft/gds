package com.gillsoft.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(description = "The document with file in base64 string")
public class Document implements Serializable {

	private static final long serialVersionUID = -6285428228882736147L;

	@ApiModelProperty("Type")
	private DocumentType type;

	@ApiModelProperty("The file in base64 string")
	private String base64;

	public Document() {

	}
	
	public Document(DocumentType type, String base64) {
		this.type = type;
		this.base64 = base64;
	}

	public DocumentType getType() {
		return type;
	}

	public void setType(DocumentType type) {
		this.type = type;
	}

	public String getBase64() {
		return base64;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

}
