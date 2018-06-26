package com.gillsoft.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.core.OrderController;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/order")
@Api(tags = { "Order" }, produces = "application/json")
public class OrderApiController {
	
	@Autowired
	private OrderController controller;
	
	@ApiOperation(value = "Return new order created from selected services",
			response = OrderResponse.class)
	@PostMapping
	public OrderResponse create(@Validated @RequestBody OrderRequest request) {
		return controller.create(request);
	}
	
	@ApiOperation(value = "Confirm selected orders and return its",
			response = OrderResponse.class)
	@PostMapping("/confirm")
	public List<OrderResponse> confirm(@Validated @RequestBody List<OrderRequest> request) {
		return controller.confirm(request);
	}
	
	@ApiOperation(value = "Confirm selected orders and return its",
			response = OrderResponse.class)
	@PostMapping("/return/prepare")
	public List<OrderResponse> prepareReturn(@Validated @RequestBody List<OrderRequest> request) {
		return controller.confirm(request);
	}

}
