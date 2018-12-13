package com.gillsoft.control.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.control.core.OrderController;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/order")
@Api(tags = { "Order" }, produces = "application/json", consumes = "application/json")
public class OrderApiController {

	@Autowired
	private OrderController controller;
	
	@ApiOperation(value = "Return new order created from selected services",
			response = OrderResponse.class)
	@PostMapping
	public OrderResponse create(@Validated @RequestBody OrderRequest request) {
		return controller.create(request);
	}
	
	@ApiOperation(value = "Confirm selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/confirm")
	public OrderResponse confirm(@PathVariable long orderId) {
		return controller.confirm(orderId);
	}
	
	@ApiOperation(value = "Cacnel selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/cancel")
	public OrderResponse cancel(@PathVariable long orderId) {
		return null;
	}
	
	@ApiOperation(value = "Returns the information about selected order",
			response = OrderResponse.class)
	@GetMapping("/{orderId}")
	public OrderResponse get(@PathVariable long orderId) {
		return controller.getOrder(orderId);
	}
	
	@ApiOperation(value = "Returns the information about selected service",
			response = OrderResponse.class)
	@GetMapping("/service/{serviceId}")
	public OrderResponse getService(@PathVariable long serviceId) {
		return null;
	}
	
}
