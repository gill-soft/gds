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
import com.gillsoft.model.RestError;
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
		return getResponse(requests, (orderRequest) -> {
			return store.getResourceService(orderRequest.getParams())
					.getOrderService().confirm(orderRequest.getOrderId());
		});
	}
	
	public List<OrderResponse> booking(List<OrderRequest> requests) {
		return getResponse(requests, (orderRequest) -> {
			return store.getResourceService(orderRequest.getParams())
					.getOrderService().booking(orderRequest.getOrderId());
		});
	}
	
	public List<OrderResponse> prepareReturn(List<OrderRequest> requests) {
		return getResponse(requests, (orderRequest) -> {
			return store.getResourceService(orderRequest.getParams())
					.getOrderService().prepareReturnServices(orderRequest);
		});
	}
	
	public List<OrderResponse> confirmReturn(List<OrderRequest> requests) {
		return getResponse(requests, (orderRequest) -> {
			return store.getResourceService(orderRequest.getParams())
					.getOrderService().returnServices(orderRequest);
		});
	}
	
	private List<OrderResponse> getResponse(List<OrderRequest> requests, ResponseCreator creator) {
		List<Callable<OrderResponse>> callables = new ArrayList<>();
		for (final OrderRequest orderRequest : requests) {
			callables.add(() -> {
				try {
					activity.check(orderRequest, 5);
					OrderResponse response = creator.create(orderRequest);
					response.setId(orderRequest.getId());
					return response;
				} catch (Exception e) {
					return new OrderResponse(orderRequest.getId(), e);
				}
			});
		}
		return ThreadPoolStore.getResult(PoolType.ORDER, callables);
	}
	
	public List<OrderResponse> cancel(List<OrderRequest> requests) {
		return getResponse(requests, (orderRequest) -> {
			return store.getResourceService(orderRequest.getParams())
					.getOrderService().cancel(orderRequest.getOrderId());
		});
	}
	
	public List<OrderResponse> get(List<OrderRequest> requests) {
		return getResponse(requests, (orderRequest) -> {
			return store.getResourceService(orderRequest.getParams())
					.getOrderService().get(orderRequest.getOrderId());
		});
	}
	
	public List<OrderResponse> getService(List<OrderRequest> requests) {
		return getResponse(requests, (orderRequest) -> {
			OrderResponse response = new OrderResponse();
			response.setId(orderRequest.getId());
			for (ServiceItem service : orderRequest.getServices()) {
				try {
					OrderResponse serviceResponse = store.getResourceService(orderRequest.getParams())
						.getOrderService().getService(service.getId());
					
					// складываем все данные в общий ответ по ресурсу
					// пункты
					if (serviceResponse.getLocalities() != null) {
						if (response.getLocalities() == null) {
							response.setLocalities(serviceResponse.getLocalities());
						} else {
							response.getLocalities().putAll(serviceResponse.getLocalities());
						}
					}
					// транспорт
					if (serviceResponse.getVehicles() != null) {
						if (response.getVehicles() == null) {
							response.setVehicles(serviceResponse.getVehicles());
						} else {
							response.getVehicles().putAll(serviceResponse.getVehicles());
						}
					}
					// организации
					if (serviceResponse.getOrganisations() != null) {
						if (response.getOrganisations() == null) {
							response.setOrganisations(serviceResponse.getOrganisations());
						} else {
							response.getOrganisations().putAll(serviceResponse.getOrganisations());
						}
					}
					// пассажиры
					if (serviceResponse.getCustomers() != null) {
						if (response.getCustomers() == null) {
							response.setCustomers(serviceResponse.getCustomers());
						} else {
							response.getCustomers().putAll(serviceResponse.getCustomers());
						}
					}
					// рейсы
					if (serviceResponse.getSegments() != null) {
						if (response.getSegments() == null) {
							response.setSegments(serviceResponse.getSegments());
						} else {
							response.getSegments().putAll(serviceResponse.getSegments());
						}
					}
					// дополнительные данные
					if (serviceResponse.getAdditionals() != null) {
						if (response.getAdditionals() == null) {
							response.setAdditionals(serviceResponse.getAdditionals());
						} else {
							response.getAdditionals().putAll(serviceResponse.getAdditionals());
						}
					}
					// билеты/сервисы
					if (serviceResponse.getServices() != null) {
						if (response.getServices() == null) {
							response.setServices(serviceResponse.getServices());
						} else {
							response.getServices().addAll(serviceResponse.getServices());
						}
					}
				} catch (Exception e) {
					
					// билеты/сервисы c ошибкой
					if (response.getServices() == null) {
						response.setServices(new ArrayList<>());
					}
					ServiceItem serviceItem = new ServiceItem();
					serviceItem.setError(new RestError(e.getMessage()));
					response.getServices().add(serviceItem);
				}
			}
			return response;
		});
	}
	
	public List<OrderResponse> getDocuments(List<OrderRequest> requests) {
		return getResponse(requests, (orderRequest) -> {
			return store.getResourceService(orderRequest.getParams())
					.getOrderService().getPdfDocuments(orderRequest);
		});
	}
	
	private interface ResponseCreator {
		
		public OrderResponse create(OrderRequest request);
		
	}

}
