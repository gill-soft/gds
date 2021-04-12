package com.gillsoft.control.core;

import java.util.List;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

@Component
public class AgragatorOrderController {
	
	@Autowired
	private List<AgregatorOrderService> agregatorOrderServices;

	public OrderResponse create(OrderRequest request) {
		request = (OrderRequest) SerializationUtils.deserialize(SerializationUtils.serialize(request));
		OrderResponse response = null;
		for (AgregatorOrderService orderService : agregatorOrderServices) {
			OrderResponse orderResponse = orderService.create(request);
			if (orderResponse != null
					&& !orderResponse.getResources().isEmpty()) {
				if (response != null) {
					if (response.getResources() != null) {
						response.getResources().addAll(orderResponse.getResources());
					} else {
						response.setResources(orderResponse.getResources());
					}
					response.join(orderResponse);
				} else {
					response = orderResponse;
				}
			}
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	private List<OrderResponse> orderOperation(List<OrderRequest> requests,
			BiFunction<AgregatorOrderService, List<OrderRequest>, List<OrderResponse>> serviceOperation) {
		requests = (List<OrderRequest>) SerializationUtils.deserialize(SerializationUtils.serialize(requests));
		List<OrderResponse> responses = null;
		for (AgregatorOrderService orderService : agregatorOrderServices) {
			List<OrderResponse> orderResponses = serviceOperation.apply(orderService, requests);
			if (orderResponses != null
					&& !orderResponses.isEmpty()) {
				if (responses != null) {
					responses.addAll(orderResponses);
				} else {
					responses = orderResponses;
				}
			}
		}
		return responses;
	}

	public List<OrderResponse> addServices(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.addServices(r));
	}

	public List<OrderResponse> removeServices(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.removeServices(r));
	}

	public List<OrderResponse> updateCustomers(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.updateCustomers(r));
	}

	public List<OrderResponse> get(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.get(r));
	}

	public List<OrderResponse> getService(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.getService(r));
	}

	public List<OrderResponse> booking(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.booking(r));
	}

	public List<OrderResponse> confirm(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.confirm(r));
	}

	public List<OrderResponse> cancel(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.cancel(r));
	}

	public List<OrderResponse> prepareReturnServices(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.prepareReturnServices(r));
	}

	public List<OrderResponse> returnServices(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.returnServices(r));
	}

	public List<OrderResponse> getPdfDocuments(List<OrderRequest> requests) {
		return orderOperation(requests, (s, r) -> s.getPdfDocuments(r));
	}
	
}
