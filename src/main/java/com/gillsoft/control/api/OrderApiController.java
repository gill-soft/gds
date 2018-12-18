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
		return controller.cancel(orderId);
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
		return controller.getService(serviceId);
	}
	
	@ApiOperation(value = "Book selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/booking")
	public OrderResponse booking(@PathVariable long orderId) {
		return controller.booking(orderId);
	}
	
	@ApiOperation(value = "Prepare selected services to return operation and return its",
			response = OrderResponse.class)
	@PostMapping("/return/prepare")
	public OrderResponse prepareReturnServices(@Validated @RequestBody OrderRequest request) {
		return null;
	}
	
	@ApiOperation(value = "Confirm return operation of selected services and return its",
			response = OrderResponse.class)
	@PostMapping("/return/confirm")
	public OrderResponse returnServices(@Validated @RequestBody OrderRequest request) {
		return null;
	}
	
	@ApiOperation(value = "Add new services to selected order and return result order",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/service/add")
	public OrderResponse addServices(@PathVariable long orderId, @Validated @RequestBody OrderRequest request) {
		return controller.addService(orderId, request);
	}
	
	@ApiOperation(value = "Remove selected services from selected order and return result order",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/service/remove")
	public OrderResponse removeServices(@PathVariable long orderId, @RequestBody OrderRequest request) {
		return null;
	}
	
}
