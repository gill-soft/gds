package com.gillsoft.control.api;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.control.core.OrderController;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.model.PaymentMethod;
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
	@PostMapping("/{orderId}/confirm/{payment}")
	public OrderResponse confirm(@Validated @PathVariable long orderId, @PathVariable PaymentMethod payment) {
		return controller.confirm(orderId, payment);
	}
	
	@ApiOperation(value = "Cacnel selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/cancel")
	public OrderResponse cancel(@Validated @PathVariable long orderId) {
		return controller.cancel(orderId);
	}
	
	@ApiOperation(value = "Returns the information about selected order",
			response = OrderResponse.class)
	@GetMapping("/{orderId}")
	public OrderResponse get(@Validated @PathVariable long orderId) {
		return controller.getOrder(orderId);
	}
	
	@ApiOperation(value = "Returns the information about selected service",
			response = OrderResponse.class)
	@GetMapping("/service/{serviceId}")
	public OrderResponse getService(@Validated @PathVariable long serviceId) {
		return controller.getService(serviceId);
	}
	
	@ApiOperation(value = "Book selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/booking")
	public OrderResponse booking(@Validated @PathVariable long orderId) {
		return controller.booking(orderId);
	}
	
	@ApiOperation(value = "Calculate refund summs of selected services and return its",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/return/calc")
	public OrderResponse calcReturnServices(@Validated @PathVariable long orderId, @Validated @RequestBody OrderRequest request) {
		return controller.calcReturn(orderId, request);
	}
	
	@ApiOperation(value = "Confirm return operation of selected services and return its",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/return/confirm")
	public OrderResponse returnServices(@Validated @PathVariable long orderId, @Validated @RequestBody OrderRequest request) {
		return controller.confirmReturn(orderId, request);
	}
	
	@ApiOperation(value = "Add new services to selected order and return result order",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/service/add")
	public OrderResponse addServices(@Validated @PathVariable long orderId, @Validated @RequestBody OrderRequest request) {
		return controller.addService(orderId, request);
	}
	
	@ApiOperation(value = "Remove selected services from selected order and return result order",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/service/remove")
	public OrderResponse removeServices(@Validated @PathVariable long orderId, @RequestBody OrderRequest request) {
		return controller.removeService(orderId, request);
	}
	
	@ApiOperation(value = "Returns the pdf document of selected order",
			response = OrderResponse.class)
	@GetMapping("/{orderId}/document")
	public OrderResponse getDocuments(@Validated @PathVariable long orderId) {
		return controller.getDocuments(orderId);
	}
	
	@ApiOperation(value = "Returns the pdf document of selected order",
			responseContainer = "List", response = OrderResponse.class)
	@GetMapping("/first/{count}")
	public List<Order> getOrders(@Validated @PathVariable int count) {
		return controller.getOrders(count);
	}
	
	@ApiOperation(value = "Report statuses with selected ids")
	@PostMapping("/report")
	public void reportStatuses(@RequestBody Set<Long> ids) {
		controller.reportStatuses(ids);
	}
	
}
