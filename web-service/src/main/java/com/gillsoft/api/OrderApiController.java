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
	
	@ApiOperation(value = "Confirm selected orders and return its",
			response = OrderResponse.class, responseContainer="List")
	@PostMapping("/confirm")
	public List<OrderResponse> confirm(@Validated @RequestBody List<OrderRequest> request) {
		return controller.confirm(request);
	}
	
	@ApiOperation(value = "Book selected orders and return its",
			response = OrderResponse.class, responseContainer="List")
	@PostMapping("/booking")
	public List<OrderResponse> booking(@Validated @RequestBody List<OrderRequest> request) {
		return controller.booking(request);
	}
	
	@ApiOperation(value = "Prepare selected services to return operation and return its",
			response = OrderResponse.class, responseContainer="List")
	@PostMapping("/return/prepare")
	public List<OrderResponse> prepareReturn(@Validated @RequestBody List<OrderRequest> request) {
		return controller.prepareReturn(request);
	}
	
	@ApiOperation(value = "Confirm return operation of selected services and return its",
			response = OrderResponse.class, responseContainer="List")
	@PostMapping("/return/confirm")
	public List<OrderResponse> confirmReturn(@Validated @RequestBody List<OrderRequest> request) {
		return controller.confirmReturn(request);
	}
	
	@ApiOperation(value = "Cacnel selected orders and return its",
			response = OrderResponse.class, responseContainer="List")
	@PostMapping("/cancel")
	public List<OrderResponse> cancel(@Validated @RequestBody List<OrderRequest> request) {
		return controller.cancel(request);
	}
	
	@ApiOperation(value = "Returns the information about selected orders",
			response = OrderResponse.class, responseContainer="List")
	@PostMapping("/info")
	public List<OrderResponse> getOrder(@Validated @RequestBody List<OrderRequest> request) {
		return controller.get(request);
	}
	
	@ApiOperation(value = "Returns the information about selected services",
			response = OrderResponse.class, responseContainer="List")
	@PostMapping("/service")
	public List<OrderResponse> getService(@Validated @RequestBody List<OrderRequest> request) {
		return controller.getService(request);
	}

}
