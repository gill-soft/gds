package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

@Component
public class AgregatorOrderController {
	
	@Autowired
	private List<AgregatorOrderService> agregatorOrderServices;

	public OrderResponse create(OrderRequest request) {
		OrderRequest copy = (OrderRequest) SerializationUtils.deserialize(SerializationUtils.serialize(request));
		List<Callable<OrderResponse>> callables = new ArrayList<>(agregatorOrderServices.size());
		for (AgregatorOrderService orderService : agregatorOrderServices) {
			callables.add(() -> {
				return orderService.create(copy);
			});
		}
		List<OrderResponse> responses = ThreadPoolStore.getResult(PoolType.ORDER, callables);
		OrderResponse response = null;
		for (OrderResponse orderResponse : responses) {
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
		List<OrderRequest> copy = (List<OrderRequest>) SerializationUtils.deserialize(SerializationUtils.serialize(requests));
		List<Callable<List<OrderResponse>>> callables = new ArrayList<>(agregatorOrderServices.size());
		for (AgregatorOrderService orderService : agregatorOrderServices) {
			callables.add(() -> {
				return serviceOperation.apply(orderService, copy);
			});
		}
		List<List<OrderResponse>> results = ThreadPoolStore.getResult(PoolType.ORDER, callables);
		List<OrderResponse> responses = null;
		for (List<OrderResponse> orderResponses : results) {
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
