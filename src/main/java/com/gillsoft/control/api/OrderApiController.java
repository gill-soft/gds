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
import com.gillsoft.control.core.ThirdPartyOrderController;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.model.Lang;
import com.gillsoft.model.PaymentMethod;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/order")
@Api(tags = { "Order" }, produces = "application/json", consumes = "application/json")
public class OrderApiController {

	@Autowired
	private OrderController controller;
	
	@Autowired
	private ThirdPartyOrderController thirdPartyController;
	
	@ApiOperation(value = "Return new order created from selected services",
			response = OrderResponse.class)
	@PostMapping
	public OrderResponse create(@Validated @RequestBody OrderRequest request) {
		return controller.create(request);
	}
	
	@ApiOperation(value = "Confirm selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/confirm/{payment}")
	public OrderResponse confirm(
			@ApiParam(value = "The order id to confirm", required = true) @Validated @PathVariable long orderId,
			@ApiParam(value = "The paymant method", required = true) @PathVariable PaymentMethod payment) {
		return controller.confirm(orderId, payment);
	}
	
	@ApiOperation(value = "Cacnel selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/cancel/{cancelReason}")
	public OrderResponse cancel(
			@ApiParam(value = "The order id to cancel", required = true) @Validated @PathVariable long orderId,
			@ApiParam(value = "The cancel reason", required = true) @PathVariable String cancelReason) {
		return controller.cancel(orderId, cancelReason);
	}
	
	@ApiOperation(value = "Returns the information about selected order",
			response = OrderResponse.class)
	@GetMapping("/{orderId}")
	public OrderResponse get(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId) {
		return controller.getOrder(orderId);
	}
	
	@ApiOperation(value = "Returns the information about selected order",
			response = OrderResponse.class)
	@GetMapping("/{orderId}/{lang}")
	public OrderResponse get(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId,
			@ApiParam(value = "The lang of requested data", required = true) @PathVariable(required = true) Lang lang) {
		return controller.getOrder(orderId);//TODO return on selected lang
	}
	
	@ApiOperation(value = "Returns the information about selected service",
			response = OrderResponse.class)
	@GetMapping("/service/{serviceId}")
	public OrderResponse getService(
			@ApiParam(value = "The selected service id", required = true) @Validated @PathVariable long serviceId) {
		return controller.getService(serviceId);
	}
	
	@ApiOperation(value = "Returns the information about selected service",
			response = OrderResponse.class)
	@GetMapping("/service/{serviceId}/{lang}")
	public OrderResponse getService(
			@ApiParam(value = "The selected service id", required = true) @Validated @PathVariable long serviceId,
			@ApiParam(value = "The lang of requested data", required = true) @PathVariable(required = true) Lang lang) {
		return controller.getService(serviceId);//TODO return on selected lang
	}
	
	@ApiOperation(value = "Book selected order and return it",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/booking")
	public OrderResponse booking(
			@ApiParam(value = "The order id to booking", required = true) @Validated @PathVariable long orderId) {
		return controller.booking(orderId);
	}
	
	@ApiOperation(value = "Calculate refund summs of selected services and return its",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/return/calc")
	public OrderResponse calcReturnServices(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId,
			@Validated @RequestBody OrderRequest request) {
		return controller.calcReturn(orderId, request);
	}
	
	@ApiOperation(value = "Confirm return operation of selected services and return its",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/return/confirm")
	public OrderResponse returnServices(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId,
			@Validated @RequestBody OrderRequest request) {
		return controller.confirmReturn(orderId, request);
	}
	
	@ApiOperation(value = "Add new services to selected order and return result order",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/service/add")
	public OrderResponse addServices(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId,
			@Validated @RequestBody OrderRequest request) {
		return controller.addService(orderId, request);
	}
	
	@ApiOperation(value = "Remove selected services from selected order and return result order",
			response = OrderResponse.class)
	@PostMapping("/{orderId}/service/remove")
	public OrderResponse removeServices(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId, 
			@Validated @RequestBody OrderRequest request) {
		return controller.removeService(orderId, request);
	}
	
	@ApiOperation(value = "Returns the pdf document of selected order",
			response = OrderResponse.class)
	@GetMapping("/{orderId}/document")
	public OrderResponse getDocuments(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId) {
		return controller.getDocuments(orderId, null);
	}
	
	@ApiOperation(value = "Returns the pdf document of selected order",
			response = OrderResponse.class)
	@GetMapping("/{orderId}/document/{lang}")
	public OrderResponse getDocuments(
			@ApiParam(value = "The selected order id", required = true) @Validated @PathVariable long orderId,
			@ApiParam(value = "The lang of requested data", required = true) @PathVariable(required = true) Lang lang) {
		return controller.getDocuments(orderId, lang);
	}
	
	@ApiOperation(value = "Returns the list of orders",
			responseContainer = "List", response = OrderResponse.class, hidden = true)
	@GetMapping("/first/{count}")
	public List<Order> getOrders(@Validated @PathVariable int count) {
		return controller.getOrders(count);
	}
	
	@ApiOperation(value = "Report statuses with selected ids", hidden = true)
	@PostMapping("/report")
	public void reportStatuses(@RequestBody Set<Long> ids) {
		controller.reportStatuses(ids);
	}
	
	@ApiOperation(value = "Return new order created from selected services", hidden = true)
	@PostMapping("/save_update")
	public void saveOrUpdate(@Validated @RequestBody List<OrderResponse> responses) {
		thirdPartyController.saveOrUpdate(responses);
	}
	
}
