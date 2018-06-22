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
			for (ServiceItem item : request.getServices()) {
				requestCustomers.put(item.getCustomer().getId(), customers.get(item.getCustomer().getId()));
			}
			orderRequest.setCustomers(requestCustomers);
			callables.add(() -> {
				activity.check(request, 5);
				OrderResponse response = store.getResourceService(request.getParams()).getOrderService().create(request);
				response.setId(request.getId());
				return response;
			});
		}
		OrderResponse response = new OrderResponse();
		response.setCustomers(customers);
		response.setResources(ThreadPoolStore.getResult(PoolType.ORDER, callables));
		return response;
	}

}
