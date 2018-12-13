package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.gillsoft.model.RestError;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.User;
import com.gillsoft.util.StringUtil;

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
		order.setResponse(result);
		
		User user = dataController.getUser();
		
		// преобразовываем ответ
		for (OrderResponse orderResponse : response.getResources()) {
			Stream<OrderRequest> stream = createRequest.getResources().stream().filter(r -> r.getId().equals(orderResponse.getId()));
			if (stream != null) {
				
				// запрос, по которому получен результат
				OrderRequest currRequest = stream.findFirst().get();
				
				// заказы ресурсов для сохранения
				ResourceOrder resourceOrder = new ResourceOrder();
				resourceOrder.setResourceId(currRequest.getParams().getResource().getId());
				resourceOrder.setResourceNativeOrderId(
						new IdModel(resourceOrder.getId(), orderResponse.getOrderId()).asString());
				order.addResourceOrder(resourceOrder);
				if (orderResponse.getError() != null) {
					currRequest.getServices().forEach(s -> s.setError(orderResponse.getError()));
					result.getServices().addAll(currRequest.getServices());
					
					// сервисы ресурса для сохранения
					currRequest.getServices().forEach(s -> {
						resourceOrder.addResourceService(createResourceService(created, user, resourceOrder.getId(), s, Status.NEW_ERROR,
								orderResponse.getError().getMessage()));
					});
				} else {
					for (ServiceItem item : orderResponse.getServices()) {
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
							resourceOrder.addResourceService(createResourceService(created, user, resourceOrder.getId(), item, Status.NEW, null));
						} else {
							resourceOrder.addResourceService(createResourceService(created, user, resourceOrder.getId(), item, Status.NEW_ERROR,
									item.getError().getMessage()));
						}
						result.getServices().add(item);
					}
				}
			}
		}
		return order;
	}
	
	private ResourceService createResourceService(Date created, User user, long resourceId, ServiceItem service, Status statusType, String error) {
		ResourceService resourceService = new ResourceService();
		if (service.getId() == null
				|| service.getId().isEmpty()) {
			service.setId(StringUtil.generateUUID());
		}
		service.setId(new IdModel(resourceId, service.getId()).asString());
		resourceService.setResourceNativeServiceId(service.getId());
		resourceService.addStatus(createStatus(created, user, statusType, error));
		return resourceService;
	}
	
	private ServiceStatus createStatus(Date created, User user, Status statusType, String error) {
		ServiceStatus status = new ServiceStatus();
		status.setCreated(created);
		status.setStatus(statusType);
		status.setUserId(user.getId());
		status.setOrganisationId(user.getParents().iterator().next().getId());
		status.setError(error);
		return status;
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
							service.setStatus(getLastStatus(resourceService.getStatuses()).name());
							break outer;
						}
					}
				}
			}
		}
		return response;
	}
	
	public OrderResponse convertToConfirm(Order order, List<OrderRequest> requests, List<OrderResponse> responses) {
		List<ServiceItem> services = new ArrayList<>();
		Date created = new Date();
		User user = dataController.getUser();
		
		// преобразовываем ответ
		for (OrderResponse orderResponse : responses) {
			Stream<OrderRequest> stream = requests.stream().filter(r -> r.getId().equals(orderResponse.getId()));
			if (stream != null) {
				
				// запрос, по которому получен результат
				OrderRequest currRequest = stream.findFirst().get();
				
				// обрабатываем ошибку заказ
				if (orderResponse.getError() != null) {
					for (ResourceOrder resourceOrder : order.getOrders()) {
						if (Objects.equals(currRequest.getOrderId(), new IdModel().create(resourceOrder.getResourceNativeOrderId()).getId())) {
							for (ResourceService resourceService : resourceOrder.getServices()) {
								ServiceItem item = new ServiceItem();
								item.setConfirmed(false);
								item.setId(resourceService.getResourceNativeServiceId());
								item.setError(orderResponse.getError());
								services.add(item);
								
								// добавляем статус об ошибке
								resourceService.addStatus(createStatus(created, user, Status.CONFIRM_ERROR, orderResponse.getError().getMessage()));
							}
							break;
						}
					}
				} else {
					
					// формируем ответ
					for (ResourceOrder resourceOrder : order.getOrders()) {
						if (Objects.equals(currRequest.getOrderId(), new IdModel().create(resourceOrder.getResourceNativeOrderId()).getId())) {
							for (ServiceItem service : orderResponse.getServices()) {
								for (ResourceService resourceService : resourceOrder.getServices()) {
									if (Objects.equals(service.getId(), new IdModel().create(resourceService.getResourceNativeServiceId()).getId())) {
										service.setId(resourceService.getResourceNativeServiceId());
										services.add(service);
										
										if (!service.getConfirmed()
												&& service.getError() == null) {
											service.setError(new RestError("Error when confirm order"));
										}
										// добавляем статус
										resourceService.addStatus(createStatus(created, user,
												service.getConfirmed() ? Status.CONFIRM : Status.CONFIRM_ERROR,
														service.getError() == null ? null : service.getError().getMessage()));
										break;
									}
								}
							}
							break;
						}
					}
					
				}
			}
		}
		return createResponse(order, services);
	}
	
	private OrderResponse createResponse(Order order, List<ServiceItem> services) {
		OrderResponse response = new OrderResponse();
		response.setOrderId(String.valueOf(order.getId()));
		response.setServices(new ArrayList<>());
		
		//TODO обновляем полученные данные в самом OrderResponse, который в базе
		for (ServiceItem dbItem : order.getResponse().getServices()) {
			outer:
			for (ServiceItem service : services) {
				if (Objects.equals(dbItem.getId(), service.getId())) {
					
					ServiceItem responseItem = null;
					// данные с ошибками не обновляем
					if (service.getError() == null) {
						//TODO update response data in db
						responseItem = dbItem;
					} else {
						responseItem = service;
					}
					for (ResourceOrder resourceOrder : order.getOrders()) {
						for (ResourceService resourceService : resourceOrder.getServices()) {
							if (Objects.equals(responseItem.getId(), resourceService.getResourceNativeServiceId())) {
								responseItem.setId(String.valueOf(resourceService.getId()));
								responseItem.setStatus(getLastStatus(resourceService.getStatuses()).name());
								response.getServices().add(responseItem);
								break outer;
							}
						}
					}
				}
			}
		}
		return response;
	}
	
	private Status getLastStatus(Set<ServiceStatus> statuses) {
		return statuses.stream().max(Comparator.comparing(ServiceStatus::getCreated)).get().getStatus();
	}

}
