package com.gillsoft.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.core.store.ResourceStore;
import com.gillsoft.model.Customer;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderController {
	
	@Autowired
	private ResourceStore store;
	
	@Autowired
	private ResourceActivity activity;
	
	public OrderResponse create(OrderRequest request) {
		Map<String, Customer> customers = request.getCustomers();
		List<Callable<OrderResponse>> callables = new ArrayList<>();
		for (final OrderRequest orderRequest : request.getResources()) {
			
			// выбираем закзчиков только выбранного ресурса
			Map<String, Customer> requestCustomers = new HashMap<>();
			for (ServiceItem item : orderRequest.getServices()) {
				requestCustomers.put(item.getCustomer().getId(), customers.get(item.getCustomer().getId()));
			}
			orderRequest.setCustomers(requestCustomers);
			callables.add(() -> {
				try {
					activity.check(orderRequest, 5);
					OrderResponse response = store.getResourceService(orderRequest.getParams())
							.getOrderService().create(orderRequest);
					response.setId(orderRequest.getId());
					return response;
				} catch (Exception e) {
					return new OrderResponse(orderRequest.getId(), e);
				}
			});
		}
		OrderResponse response = new OrderResponse();
		response.setCustomers(customers);
		response.setResources(ThreadPoolStore.getResult(PoolType.ORDER, callables));
		
		// удаляем пассажиров с заказов ресурса так как они в общем ответе
		for (OrderResponse orderResponse : response.getResources()) {
			orderResponse.setCustomers(null);
		}
		return response;
	}
	
	public List<OrderResponse> confirm(List<OrderRequest> requests) {
		List<Callable<OrderResponse>> callables = new ArrayList<>();
		for (final OrderRequest orderRequest : requests) {
			callables.add(() -> {
				try {
					activity.check(orderRequest, 5);
					OrderResponse response = store.getResourceService(orderRequest.getParams())
							.getOrderService().confirm(orderRequest.getOrderId());
					response.setId(orderRequest.getId());
					return response;
				} catch (Exception e) {
					return new OrderResponse(orderRequest.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.ORDER, callables);
	}
	
	public List<OrderResponse> prepareReturn(List<OrderRequest> requests) {
		List<Callable<OrderResponse>> callables = new ArrayList<>();
		for (final OrderRequest orderRequest : requests) {
			callables.add(() -> {
				try {
					activity.check(orderRequest, 5);
					OrderResponse response = store.getResourceService(orderRequest.getParams())
							.getOrderService().prepareReturnServices(orderRequest);
					response.setId(orderRequest.getId());
					return response;
				} catch (Exception e) {
					return new OrderResponse(orderRequest.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.ORDER, callables);
	}

}
