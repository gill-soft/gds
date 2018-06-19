package com.gillsoft.abstract_rest_service;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.gillsoft.model.Method;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.model.service.OrderService;

public abstract class AbstractOrderService implements OrderService {

	@Override
	@PostMapping(Method.ORDER)
	public OrderResponse create(@RequestBody OrderRequest request) {
		return createResponse(request);
	}
	
	public abstract OrderResponse createResponse(OrderRequest request);

	@Override
	@PutMapping(Method.ORDER_TICKET)
	public OrderResponse addTickets(@RequestBody OrderRequest request) {
		return addTicketsResponse(request);
	}
	
	public abstract OrderResponse addTicketsResponse(OrderRequest request);

	@Override
	@DeleteMapping(Method.ORDER_TICKET)
	public OrderResponse removeTickets(@RequestBody OrderRequest request) {
		return removeTicketsResponse(request);
	}
	
	public abstract OrderResponse removeTicketsResponse(OrderRequest request);

	@Override
	@PatchMapping(Method.ORDER_TICKET)
	public OrderResponse updatePassengers(@RequestBody OrderRequest request) {
		return updatePassengersResponse(request);
	}
	
	public abstract OrderResponse updatePassengersResponse(OrderRequest request);

	@Override
	@GetMapping(Method.ORDER)
	public OrderResponse get(@RequestParam("id") String id) {
		return getResponse(id);
	}
	
	public abstract OrderResponse getResponse(String id);
	
	@Override
	@GetMapping(Method.ORDER_TICKET)
	public OrderResponse getTicket(@RequestParam("ticketId") String ticketId) {
		return getTicketResponse(ticketId);
	}
	
	public abstract OrderResponse getTicketResponse(String ticketId);

	@Override
	@PatchMapping(Method.ORDER)
	public OrderResponse book(@RequestParam("id") String id) {
		return bookResponse(id);
	}
	
	public abstract OrderResponse bookResponse(String id);

	@Override
	@PutMapping(Method.ORDER)
	public OrderResponse pay(@RequestParam("id") String id) {
		return payResponse(id);
	}
	
	public abstract OrderResponse payResponse(String id);

	@Override
	@DeleteMapping(Method.ORDER)
	public OrderResponse cancel(@RequestParam("id") String id) {
		return cancelResponse(id);
	}
	
	public abstract OrderResponse cancelResponse(String id);

	@Override
	@PostMapping(Method.ORDER_RETURN)
	public OrderResponse returnTickets(@RequestBody OrderRequest request) {
		return returnTicketsResponse(request);
	}
	
	public abstract OrderResponse returnTicketsResponse(OrderRequest request);

	@Override
	@PostMapping(Method.ORDER_TICKET)
	public OrderResponse getPdfTickets(@RequestBody OrderRequest request) {
		return getPdfTicketsResponse(request);
	}
	
	public abstract OrderResponse getPdfTicketsResponse(OrderRequest request);

}
