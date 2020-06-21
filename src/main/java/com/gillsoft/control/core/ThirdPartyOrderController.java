package com.gillsoft.control.core;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.NoDataFoundException;
import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.ClientAccountService;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.control.service.model.ClientView;
import com.gillsoft.control.service.model.ManageException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderParams;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.model.Customer;
import com.gillsoft.model.Resource;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.response.OrderResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ThirdPartyOrderController {
	
	private static Logger LOGGER = LogManager.getLogger(ThirdPartyOrderController.class);
	
	@Autowired
	private OrderResponseConverter orderConverter;
	
	@Autowired
	private OrderDAOManager manager;
	
	@Autowired
	private OrderRequestController requestController;
	
	@Autowired
	private ClientAccountService clientService;
	
	@Autowired
	private TripSearchMapping searchMapping;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	public void saveOrUpdate(List<OrderResponse> responses) {
		for (OrderResponse orderResponse : responses) {
			try {
				Order order = findOrCreateOrder(getCopy(orderResponse));
				if (order == null) {
					throw new NoDataFoundException("Order not found and not created");
				}
				confirmOrder(getCopy(orderResponse), order);
				returOrder(getCopy(orderResponse), order);
				cancelOrder(getCopy(orderResponse), order);
			} catch (Exception e) {
				LOGGER.error(e);
			}
		}
		
	}
	
	private OrderResponse getCopy(OrderResponse orderResponse) {
		return (OrderResponse) SerializationUtils.deserialize(SerializationUtils.serialize(orderResponse));
	}
	
	private Order findOrCreateOrder(OrderResponse orderResponse) {
		Order order = null;
		try {
			return manager.getFullOrder(createFindOrderParams(orderResponse.getResources().get(0)));
		} catch (ManageException e) {
			order = orderConverter.convertToNewOrder(orderResponse);
			registerClients(orderResponse);
		}
		try {
			markUnmapped(order);
			manager.create(order);
		} catch (ManageException e) {
			LOGGER.error(e);
		}
		return order;
	}
	
	private OrderParams createFindOrderParams(OrderResponse orderResponse) {
		OrderParams params = new OrderParams();
		params.setResourceNativeOrderId(new IdModel(getResourceId(orderResponse),
				orderResponse.getOrderId()).asString());
		return params;
	}
	
	private long getResourceId(OrderResponse orderResponse) {
		if (orderResponse.getSegments() != null
				&& !orderResponse.getSegments().isEmpty()) {
			for (Segment segment : orderResponse.getSegments().values()) {
				Resource resource = segment.getResource();
				if (resource != null) {
					return orderConverter.getParamsResourceId(orderConverter.getResourceParam(resource.getId()));
				}
			}
		}
		return -1;
	}
	
	private void registerClients(OrderResponse orderResponse) {
		for (Customer customer : orderResponse.getCustomers().values()) {
			registerClient(customer);
		}
	}
	
	private void registerClient(Customer customer) {
		ThreadPoolStore.execute(PoolType.LOCALITY, () -> {
			ClientView client = new ClientView();
			client.setFields(customer);
			clientService.register(client);
		});
	}
	
	private void confirmOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForConfirm());
			
			Set<ServiceStatus> statuses = new HashSet<>();
			statuses.add(ServiceStatus.CONFIRM);
			statuses.add(ServiceStatus.RETURN);
			statuses.add(ServiceStatus.CANCEL);
			requestController.checkStatus(orderResponse.getResources(), statuses);
			
			order = orderConverter.convertToConfirm(order, orderResponse.getResources(),
					ServiceStatus.CONFIRM, ServiceStatus.CONFIRM_ERROR);
			markUnmapped(order);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error(e);
		}
	}
	
	private void returOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForReturn());
			requestController.checkStatus(orderResponse.getResources(), Collections.singleton(ServiceStatus.RETURN));
			
			order = orderConverter.convertToReturn(order, orderResponse.getResources());
			markUnmapped(order);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error(e);
		}
	}
	
	private void cancelOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForCancel());
			requestController.checkStatus(orderResponse.getResources(), Collections.singleton(ServiceStatus.CANCEL));
			
			order = orderConverter.convertToConfirm(order, orderResponse.getResources(),
					ServiceStatus.CANCEL, ServiceStatus.CANCEL_ERROR);
			markUnmapped(order);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error(e);
		}
	}
	
	private void markUnmapped(Order order) {
		if (order.getResponse().getSegments() == null) {
			return;
		}
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				for (ServiceItem service : order.getResponse().getServices()) {
					if (orderConverter.isServiceOfResourceService(service, resourceService)) {
						if (service.getSegment() != null) {
							Segment segment = order.getResponse().getSegments().get(service.getSegment().getId());
							if (segment != null
									&& segment.getTripId() == null) {
								resourceService.setMappedTrip(false);
							}
						}
						break;
					}
				}
			}
		}
	}
	
	@Scheduled(initialDelay = 10000, fixedDelay = 300000)
	private void mapUnmappedSegments() {
		defaultAuth();
		OrderParams params = new OrderParams();
		params.setMappedTrip(false);
		params.setMappedDeparture(new Date()); //TODO convert to departure city timezone
		try {
			List<Order> orders = manager.getOrders(params);
			
			// группируем сегменты всех заказов по ид ресурса
			Map<String, Map<String, Segment>> grouped = groupTripsByResource(orders);
			
			// маппим рейсы
			for (Map<String, Segment> groupe : grouped.values()) {
				searchMapping.mapSegmentsTripId(groupe);
			}
			// обновляем смапленные заказы
			updateByMappedTrips(orders, grouped);
		} catch (ManageException e) {
			LOGGER.error("Get orders error in db", e);
		}
	}
	
	private void defaultAuth() {
		Authentication auth = new UsernamePasswordAuthenticationToken(Config.getMsLogin(), Config.getMsPassword());
		auth = authenticationManager.authenticate(auth);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
	
	private Map<String, Map<String, Segment>> groupTripsByResource(List<Order> orders) {
		Map<String, Map<String, Segment>> grouped = new HashMap<>();
		for (Order order : orders) {
			if (order.getResponse() != null
					&& order.getResponse().getSegments() != null) {
				for (Entry<String, Segment> entry : order.getResponse().getSegments().entrySet()) {
					Segment segment = entry.getValue();
					if (segment.getTripId() == null) {
						String segmentId = entry.getKey();
						String resourceId = segment.getResource().getId();
						Map<String, Segment> group = grouped.get(resourceId);
						if (group == null) {
							group = new HashMap<>();
							grouped.put(resourceId, group);
						}
						group.put(segmentId, segment);
					}
				}
			}
		}
		return grouped;
	}
	
	private void updateByMappedTrips(List<Order> orders, Map<String, Map<String, Segment>> groupedSegments) {
		for (Order order : orders) {
			if (order.getResponse() != null
					&& order.getResponse().getSegments() != null) {
				List<ResourceService> services = new LinkedList<>();
				for (Entry<String, Segment> entry : order.getResponse().getSegments().entrySet()) {
					Segment segment = entry.getValue();
					String segmentId = entry.getKey();
					String resourceId = segment.getResource().getId();
					Map<String, Segment> segments = groupedSegments.get(resourceId);
					if (segments != null) {
						Segment mapped = segments.get(segmentId);
						if (mapped != null
								&& mapped.getTripId() != null) {
							services.addAll(getServicesOfSegment(segmentId, order));
						}
					}
				}
				if (!services.isEmpty()) {
					saveUpdated(order, services);
				}
			}
		}
	}
	
	private List<ResourceService> getServicesOfSegment(String segmentId, Order order) {
		List<ResourceService> services = new LinkedList<>();
		for (ServiceItem service : order.getResponse().getServices()) {
			if (service.getSegment() != null
					&& Objects.equals(service.getSegment().getId(), segmentId)) {
				for (ResourceOrder resourceOrder : order.getOrders()) {
					for (ResourceService resourceService : resourceOrder.getServices()) {
						if (orderConverter.isServiceOfResourceService(service, resourceService)) {
							services.add(resourceService);
						}
					}
				}
			}
		}
		return services;
	}
	
	private void saveUpdated(Order order, List<ResourceService> services) {
		try {
			manager.updateOrderResponse(order);
			for (ResourceService service : services) {
				try {
					manager.markResourceServiceMappedTrip(service);
				} catch (ManageException e) {
					LOGGER.error(e);
				}
			}
		} catch (ManageException e) {
			LOGGER.error(e);
		}
	}

}
