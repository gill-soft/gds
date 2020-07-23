package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import com.gillsoft.control.service.OrderDAOManager;
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
import com.gillsoft.ms.entity.Client;

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
	private ClientController clientController;
	
	@Autowired
	private NotificationController notificationController;
	
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
				addToOrder(getCopy(orderResponse), order);
				removeFromOrder(getCopy(orderResponse), order);
				confirmOrder(getCopy(orderResponse), order);
				returOrder(getCopy(orderResponse), order);
				cancelOrder(getCopy(orderResponse), order);
			} catch (Exception e) {
				LOGGER.error("saveOrUpdate error", e);
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
			try {
				markUnmapped(order);
				manager.create(order);
			} catch (ManageException e1) {
				LOGGER.error("findOrCreateOrder error");
			}
			registerClients(order);
			return order;
		}
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
	
	private void registerClients(Order order) {
		Map<String, List<String>> notifications = notificationController.createOrderNotifications(order);
		for (Entry<String, Customer> customer : order.getResponse().getCustomers().entrySet()) {
			ThreadPoolStore.execute(PoolType.LOCALITY, () -> {
				Client client = clientController.register(customer.getValue(), notifications.get(customer.getKey()));
				if (client != null) {
					try {
						manager.addOrderClient(order, client);
					} catch (ManageException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	private void addToOrder(OrderResponse orderResponse, Order order) {
		removePresent(orderResponse, order);
		if (orderResponse.getResources().isEmpty()) {
			return;
		}
		Order newOrder = orderConverter.convertToNewOrder(orderResponse);
		order = orderConverter.joinOrders(order, newOrder);
		try {
			markUnmapped(order);
			manager.addServices(order);
		} catch (ManageException e) {
			LOGGER.error("Add services to order error in db", e);
		}
	}
	
	private void removePresent(OrderResponse orderResponse, Order order) {
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				for (OrderResponse resourceResponse : orderResponse.getResources()) {
					for (Iterator<ServiceItem> iterator = resourceResponse.getServices().iterator(); iterator.hasNext();) {
						ServiceItem service = iterator.next();
						if (orderConverter.isServiceOfResourceService(service, resourceService)) {
							iterator.remove();
						}
					}
				}
			}
		}
		for (Iterator<OrderResponse> iterator = orderResponse.getResources().iterator(); iterator.hasNext();) {
			OrderResponse response = iterator.next();
			if (response.getServices().isEmpty()) {
				iterator.remove();
			}
		}
	}
	
	private void removeFromOrder(OrderResponse orderResponse, Order order) {
		List<ServiceItem> servicesForRemove = getServicesForRemove(orderResponse, order); 
		if (servicesForRemove.isEmpty()) {
			return;
		}
		order = orderConverter.removeServices(order, servicesForRemove, false);
		try {
			manager.removeServices(order);
		} catch (ManageException e) {
			LOGGER.error("Remove services from order error in db", e);
		}
	}
	
	private List<ServiceItem> getServicesForRemove(OrderResponse orderResponse, Order order) {
		List<ServiceItem> services = new ArrayList<>();
		for (OrderResponse response : orderResponse.getResources()) {
			for (ResourceOrder resourceOrder : order.getOrders()) {
				IdModel idModel = new IdModel().create(resourceOrder.getResourceNativeOrderId());
				if (Objects.equals(response.getOrderId(), idModel.getId())) {
					for (ResourceService resourceService : resourceOrder.getServices()) {
						if (isNotPresentService(resourceService, response.getServices())) {
							ServiceItem forRemove = getServiceForRemove(resourceService, order.getResponse().getServices());
							if (forRemove != null) {
								services.add(forRemove);
							}
						}
					}
				}
			}
		}
		return services;
	}
	
	private boolean isNotPresentService(ResourceService resourceService, List<ServiceItem> services) {
		for (ServiceItem service : services) {
			if (orderConverter.isServiceOfResourceService(service, resourceService)) {
				return false;
			}
		}
		return true;
	}
	
	private ServiceItem getServiceForRemove(ResourceService resourceService, List<ServiceItem> services) {
		for (ServiceItem service : services) {
			if (orderConverter.isServiceOfResourceService(service, resourceService)) {
				return service;
			}
		}
		return null;
	}
	
	private void confirmOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForConfirm());
			
			Set<ServiceStatus> statusesForSave = new HashSet<>();
			statusesForSave.add(ServiceStatus.CONFIRM);
			statusesForSave.add(ServiceStatus.RETURN);
			statusesForSave.add(ServiceStatus.CANCEL);
			requestController.checkStatus(orderResponse.getResources(), statusesForSave);
			
			order = orderConverter.convertToConfirm(order, prepareResponses(order, orderResponse, statusesForSave),
					ServiceStatus.CONFIRM, ServiceStatus.CONFIRM_ERROR);
			markUnmapped(order);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error("confirmOrder error");
		}
	}
	
	private void returOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForReturn());
			
			Set<ServiceStatus> statusesForSave = Collections.singleton(ServiceStatus.RETURN);
			requestController.checkStatus(orderResponse.getResources(), statusesForSave);
			
			order = orderConverter.convertToReturn(order, prepareResponses(order, orderResponse, statusesForSave));
			markUnmapped(order);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error("returnOrder error");
		}
	}
	
	private void cancelOrder(OrderResponse orderResponse, Order order) {
		try {
			requestController.checkStatus(order, requestController.getStatusesForCancel());
			
			Set<ServiceStatus> statusesForSave = Collections.singleton(ServiceStatus.CANCEL);
			requestController.checkStatus(orderResponse.getResources(), statusesForSave);
			
			order = orderConverter.convertToConfirm(order, prepareResponses(order, orderResponse, statusesForSave),
					ServiceStatus.CANCEL, ServiceStatus.CANCEL_ERROR);
			markUnmapped(order);
			manager.confirm(order);
		} catch (MethodUnavalaibleException | ManageException e) {
			LOGGER.error("cancelOrder error");
		}
	}
	
	private List<OrderResponse> prepareResponses(Order order, OrderResponse orderResponse, Set<ServiceStatus> statusesForSave) {
		for (OrderResponse response : orderResponse.getResources()) {
			if (response.getServices() != null) {
				response.getServices().removeIf(s -> !statusesForSave.contains(ServiceStatus.valueOf(s.getStatus()))
						|| isPresentStatus(order, s));
			}
		}
		orderResponse.getResources().removeIf(r -> r.getServices() == null || r.getServices().isEmpty());
		return orderResponse.getResources();
	}
	
	private boolean isPresentStatus(Order order, ServiceItem service) {
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				if (orderConverter.isServiceOfResourceService(service, resourceService)) {
					ServiceStatus status = ServiceStatus.valueOf(service.getStatus());
					return status == orderConverter.getLastNotErrorStatus(resourceService.getStatuses());
				}
			}
		}
		return false;
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
	public void mapUnmappedSegments() {
		defaultAuth();
		OrderParams params = new OrderParams();
		params.setMappedTrip(false);
		params.setMappedDeparture(new Date(System.currentTimeMillis() - 7 * 24 * 3600000l));
		try {
			List<Order> orders = manager.getOrders(params);
			for (Order order : orders) {
				
				// группируем сегменты по ид ресурса
				Map<String, Map<String, Segment>> grouped = groupTripsByResource(order);
				
				// маппим рейсы
				for (Map<String, Segment> groupe : grouped.values()) {
					searchMapping.mapSegmentsTripId(groupe);
				}
				// обновляем смапленные заказы
				updateByMappedTrips(order, grouped);
			}
		} catch (ManageException e) {
			LOGGER.error("Get orders error in db");
		}
	}
	
	private void defaultAuth() {
		Authentication auth = new UsernamePasswordAuthenticationToken(Config.getMsLogin(), Config.getMsPassword());
		auth = authenticationManager.authenticate(auth);
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
	
	private Map<String, Map<String, Segment>> groupTripsByResource(Order order) {
		Map<String, Map<String, Segment>> grouped = new HashMap<>();
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
		return grouped;
	}
	
	private void updateByMappedTrips(Order order, Map<String, Map<String, Segment>> groupedSegments) {
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
					LOGGER.error("saveUpdated service error");
				}
			}
		} catch (ManageException e) {
			LOGGER.error("saveUpdated order error");
		}
	}

}
