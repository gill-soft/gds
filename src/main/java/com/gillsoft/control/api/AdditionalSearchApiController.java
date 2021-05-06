package com.gillsoft.control.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.control.core.AdditionalSearchController;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.request.AdditionalSearchRequest;
import com.gillsoft.model.response.AdditionalSearchResponse;
import com.gillsoft.model.response.TripSearchResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/additional")
@Api(tags = { "Additional search" }, produces = "application/json", consumes = "application/json")
public class AdditionalSearchApiController {
	
	@Autowired
	private AdditionalSearchController controller;
	
	@ApiOperation(value = "Init search process", response = TripSearchResponse.class)
	@PostMapping
	public AdditionalSearchResponse initSearch(@Validated @RequestBody AdditionalSearchRequest request) {
		return controller.initSearch(request);
	}
	
	@ApiOperation(value = "Returns part of founded additional services and link id to next result",
			response = TripSearchResponse.class)
	@GetMapping("/{searchId}")
	public AdditionalSearchResponse getSearchResult(
			@ApiParam(value = "The search id from init search request", required = true) @Validated @PathVariable String searchId) {
		return controller.getSearchResult(searchId);
	}
	
	@ApiOperation(value = "Returns the list of service tariffs",
			response = Tariff.class, responseContainer="List")
	@GetMapping("/{serviceAdditionalId}/tariffs/{lang}")
	public List<Tariff> getTariffs(
			@ApiParam(value = "The id of selected additional service", required = true) @Validated @PathVariable String serviceAdditionalId,
			@ApiParam(value = "The id of selected additional service", required = true) @PathVariable(required = false) Lang lang) {
		return controller.getTariffs(serviceAdditionalId, lang);
	}
	
	@ApiOperation(value = "Returns the list of required fields to create order to this additional service",
			response = RequiredField.class, responseContainer="List")
	@GetMapping("/{serviceAdditionalId}/required")
	public List<RequiredField> getRequiredFields(
			@ApiParam(value = "The id of selected additional service", required = true) @Validated @PathVariable String serviceAdditionalId) {
		return controller.getRequiredFields(serviceAdditionalId);
	}
	
	@ApiOperation(value = "Returns the list of tariff return conditions to this additional service and selected tariff",
			response = ReturnCondition.class, responseContainer="List")
	@GetMapping("/{serviceAdditionalId}/conditions/{tariffId}/{lang}")
	public List<ReturnCondition> getReturnConditions(
			@ApiParam(value = "The id of selected additional service", required = true) @Validated @PathVariable String serviceAdditionalId,
			@ApiParam(value = "The id of selected tariff", required = true) @Validated @PathVariable("tariffId") String tariffId,
			@ApiParam(value = "The lang of requested data", required = true) @PathVariable(required = false) Lang lang) {
		return controller.getConditions(serviceAdditionalId, tariffId, lang);
	}
	
	@ApiOperation(value = "Returns the list of some documents about additional service",
			response = Document.class, responseContainer="List")
	@GetMapping("/{serviceAdditionalId}/documents/{lang}")
	public List<Document> getDocuments(
			@ApiParam(value = "The id of selected additional service", required = true) @Validated @PathVariable String serviceAdditionalId,
			@ApiParam(value = "The lang of requested data", required = true) @PathVariable(required = false) Lang lang) {
		return controller.getDocuments(serviceAdditionalId, lang);
	}

}
