package com.gillsoft.control.core;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatus;
import com.gillsoft.control.service.model.Status;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.User;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderResponseConverter {
	
	@Autowired
	private MsDataController dataController;
	
	public Order convertToNewOrder(OrderRequest createRequest, OrderResponse result, OrderResponse response) {
		
		// заказ для сохранения
		Date created = new Date();
		Order order = new Order();
		order.setCreated(created);
		order.setOrders(new HashSet<>());
		order.setResponse(result);
		
		User user = dataController.getUser();
		
		// преобразовываем ответ
		for (OrderResponse orderResponse : response.getResources()) {
			Stream<OrderRequest> stream = createRequest.getResources().stream().filter(r -> r.getId().equals(orderResponse.getId()));
			if (stream != null) {
				
				// заказы ресурсов для сохранения
				ResourceOrder resourceOrder = new ResourceOrder();
				resourceOrder.setResourceNativeOrderId(orderResponse.getOrderId());
				resourceOrder.setServices(new HashSet<>());
				order.getOrders().add(resourceOrder);
				
				// запрос, по которому получен результат
				OrderRequest currRequest = stream.findFirst().get();
				if (orderResponse.getError() != null) {
					currRequest.getServices().forEach(s -> s.setError(orderResponse.getError()));
					result.getServices().addAll(currRequest.getServices());
					
					// сервисы ресурса для сохранения
					currRequest.getServices().forEach(s -> {
						resourceOrder.getServices().add(createResourceService(created,
								currRequest.getParams().getResource().getId(), user, s, Status.NEW_ERROR));
					});
				} else {
					for (ServiceItem item : orderResponse.getServices()) {
						Status status = Status.NEW_ERROR;
						if (item.getError() == null) {
							Segment segment = null;
							if (item.getSegment() != null) {
								setSegment(result.getSegments(), item);
								segment = result.getSegments().get(item.getSegment().getId());
							}
							// пересчитываем стоимость
							if (item.getPrice() != null) {
								item.setPrice(dataController.recalculate(
										segment != null ? segment : null, item.getPrice(), currRequest.getCurrency()));
							} else if (segment != null) {
								item.setPrice(segment.getPrice());
							}
							status = Status.NEW;
						}
						resourceOrder.getServices().add(createResourceService(created,
								currRequest.getParams().getResource().getId(), user, item, status));
						result.getServices().add(item);
					}
				}
			}
		}
		return order;
	}
	
	private ResourceService createResourceService(Date created, long resourceId, User user, ServiceItem service, Status statusType) {
		ResourceService resourceService = new ResourceService();
		resourceService.setResourceId(resourceId);
		resourceService.setResourceNativeServiceId(service.getId());
		ServiceStatus status = new ServiceStatus();
		status.setCreated(created);
		status.setStatus(statusType);
		status.setUserId(user.getId());
		status.setOrganisationId(user.getParents().iterator().next().getId());
		resourceService.setStatuses(Collections.singleton(status));
		return resourceService;
	}
	
	private void setSegment(Map<String, Segment> segments, ServiceItem item) {
		for (String id : segments.keySet()) {
			TripIdModel model = new TripIdModel().create(id);
			if (Objects.equals(item.getSegment().getId(), model.getId())) {
				item.setSegment(new Segment(id));
				break;
			}
		}
	}
	
	public OrderResponse getResponse(Order order) {
		
		// устанавливаем ид ордера в респонс и ид сервисов
		OrderResponse response = order.getResponse();
		response.setOrderId(String.valueOf(order.getId()));
		
		// ид сервисов
		for (ServiceItem service : response.getServices()) {
			outer:
			if (service.getId() != null) {
				for (ResourceOrder resourceOrder : order.getOrders()) {
					for (ResourceService resourceService : resourceOrder.getServices()) {
						if (Objects.equals(service.getId(), resourceService.getResourceNativeServiceId())) {
							service.setId(String.valueOf(resourceService.getId()));
							break outer;
						}
					}
				}
			}
		}
		return response;
	}

}
