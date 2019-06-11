package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderDocument;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.Document;
import com.gillsoft.model.DocumentType;
import com.gillsoft.model.Price;
import com.gillsoft.model.RestError;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.ServiceStatus;
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
		
		// по умолчанию время на выкуп 20 минут
		Date expire = new Date(System.currentTimeMillis() + 1200000l);
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
				resourceOrder.setResourceId(Long.parseLong(currRequest.getParams().getResource().getId()));
				resourceOrder.setResourceNativeOrderId(
						new IdModel(resourceOrder.getId(), orderResponse.getOrderId()).asString());
				order.addResourceOrder(resourceOrder);
				if (orderResponse.getError() != null) {
					currRequest.getServices().forEach(s -> s.setError(orderResponse.getError()));
					result.getServices().addAll(currRequest.getServices());
					
					// сервисы ресурса для сохранения
					currRequest.getServices().forEach(s -> {
						resourceOrder.addResourceService(createResourceService(created, user, resourceOrder.getId(), s, ServiceStatus.NEW_ERROR,
								orderResponse.getError().getMessage()));
					});
				} else {
					for (ServiceItem item : orderResponse.getServices()) {
						
						// устанавливаем ид сегмента рейса
						Segment segment = null;
						if (item.getSegment() != null
								&& result.getSegments() != null) {
							setSegment(result.getSegments(), item);
							segment = result.getSegments().get(item.getSegment().getId());
						}
						if (item.getError() == null) {
							
							// проверяем время на выкуп
							if (item.getExpire() == null) {
								item.setExpire(expire);
							}
							// проверяем возможность аннулирования заказа
							if (item.getCanceled() == null) {
								item.setCanceled(true);
							}
							// пересчитываем стоимость
							if (item.getPrice() != null) {
								item.setPrice(dataController.recalculate(
										segment != null ? segment : null, item.getPrice(), currRequest.getCurrency()));
							} else if (segment != null) {
								item.setPrice(segment.getPrice());
							}
							resourceOrder.addResourceService(createResourceService(created, user, resourceOrder.getId(), item, ServiceStatus.NEW, null));
						} else {
							resourceOrder.addResourceService(createResourceService(created, user, resourceOrder.getId(), item, ServiceStatus.NEW_ERROR,
									item.getError().getMessage()));
						}
						result.getServices().add(item);
					}
				}
			}
		}
		return order;
	}
	
	private ResourceService createResourceService(Date created, User user, long resourceId, ServiceItem service, ServiceStatus statusType, String error) {
		ResourceService resourceService = new ResourceService();
		if (service.getId() == null
				|| service.getId().isEmpty()) {
			service.setId(StringUtil.generateUUID());
		}
		service.setId(new IdModel(resourceId, service.getId()).asString());
		resourceService.setResourceNativeServiceId(service.getId());
		
		// пересчитанную стоимость сохраняем к статусу
		resourceService.addStatus(createStatus(created, user, statusType, error, service.getPrice()));
		
		// оригинальную стоимость сохраняем в response
		service.setPrice(service.getPrice().getSource());
		return resourceService;
	}
	
	private ServiceStatusEntity createStatus(Date created, User user, ServiceStatus statusType, String error, Price price) {
		ServiceStatusEntity status = new ServiceStatusEntity();
		status.setCreated(created);
		status.setStatus(statusType);
		status.setUserId(user.getId());
		status.setOrganisationId(user.getParents().iterator().next().getId());
		status.setError(error);
		status.setPrice(price);
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
		return convertResponse(order, order.getResponse());
	}
	
	private OrderResponse convertResponse(Order order, OrderResponse response) {
		response.setOrderId(String.valueOf(order.getId()));
		
		// ид сервисов
		for (ServiceItem service : response.getServices()) {
			if (service.getExpire() != null) {
				service.setExpire(convertUTCDateToUserTimeZone(service.getExpire()));
			}
			if (service.getTimeToCancel() != null) {
				service.setTimeToCancel(convertUTCDateToUserTimeZone(service.getTimeToCancel()));
			}
			if (service.getId() != null) {
				out:
					for (ResourceOrder resourceOrder : order.getOrders()) {
						for (ResourceService resourceService : resourceOrder.getServices()) {
							if (Objects.equals(service.getId(), resourceService.getResourceNativeServiceId())) {
								service.setId(String.valueOf(resourceService.getId()));
								ServiceStatus status = getLastStatus(resourceService.getStatuses());
								service.setStatus(status.name());
								
								// устанавливаем стоимость с указанного статуса
								Price price = getStatusPrice(resourceService.getStatuses(), status);
								if (price != null) {
									service.setPrice(price);
								}
								break out;
							}
						}
					}
			}
		}
		return response;
	}
	
	private Date convertUTCDateToUserTimeZone(Date date) {
		String timeZone = dataController.getUserTimeZone();
		if (timeZone != null) {
			return new Date(date.getTime() + Utils.getOffset(timeZone, date.getTime()));
		}
		return date;
	}
	
	public List<ServiceItem> joinServices(Order order, List<OrderRequest> requests, List<OrderResponse> responses,
			ServiceStatus confirmStatus, ServiceStatus errorStatus) {
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
								if (confirmStatus != null) {
									resourceService.addStatus(createStatus(created, user, confirmStatus, orderResponse.getError().getMessage(), null));
								}
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
										
										if (service.getConfirmed() != null
												&& !service.getConfirmed()
												&& service.getError() == null) {
											service.setError(new RestError("Error when " + confirmStatus + " order"));
										}
										// добавляем статус
										if (confirmStatus != null) {
											resourceService.addStatus(createStatus(created, user,
													service.getConfirmed() != null && service.getConfirmed() ? confirmStatus : errorStatus,
															service.getError() == null ? null : service.getError().getMessage(), service.getPrice()));
										}
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
		return services;
	}
	
	public Order convertToConfirm(Order order, List<OrderRequest> requests, List<OrderResponse> responses,
			ServiceStatus confirmStatus, ServiceStatus errorStatus) {
		
		// обнуляем стоимость, чтобы не переписывать созданную при заказе
		for (OrderResponse orderResponse : responses) {
			if (orderResponse.getServices() != null) {
				for (ServiceItem service : orderResponse.getServices()) {
					service.setPrice(null);
				}
			}
		}
		List<ServiceItem> services = joinServices(order, requests, responses, confirmStatus, errorStatus);
		updateResponse(order, services);
		return order;
	}
	
	public OrderResponse convertToReturnCalc(Order order, List<OrderRequest> requests, List<OrderResponse> responses) {
		OrderResponse response = new OrderResponse();
		response.setServices(joinServices(order, requests, responses, null, null));
		for (ServiceItem service : response.getServices()) {
			service.setPrice(dataController.recalculateReturn(getSegment(order, service), service.getPrice()));
		}
		return convertResponse(order, response);
	}
	
	public OrderResponse convertToReturn(Order order, List<OrderRequest> requests, List<OrderResponse> returnResponses, List<OrderResponse> calcResponses) {
		
		// пересчитываем стоимости возвратов
		for (OrderResponse orderResponse : returnResponses) {
			if (orderResponse.getServices() != null) {
				for (ServiceItem service : orderResponse.getServices()) {
					Price price = service.getPrice();
					if (price == null) {
						for (OrderResponse calcResponse : calcResponses) {
							if (calcResponse.getServices() != null) {
								Stream<ServiceItem> finded = calcResponse.getServices().stream().filter(s -> Objects.equals(s.getId(), service.getId()));
								if (finded != null) {
									price = finded.findFirst().get().getPrice();
									break;
								}
							}
						}
					}
					service.setPrice(dataController.recalculateReturn(getSegment(order, service), price));
				}
			}
		}
		OrderResponse response = new OrderResponse();
		response.setServices(joinServices(order, requests, returnResponses, ServiceStatus.RETURN, ServiceStatus.RETURN_ERROR));
		return convertResponse(order, response);
	}
	
	/*
	 * Возвращает сегмент рейса, по переданному сервису.
	 */
	private Segment getSegment(Order order, ServiceItem service) {
		String serviceId = null;
		IdModel model = new IdModel().create(service.getId());
		if (model != null) {
			serviceId = new IdModel().create(service.getId()).getId();
		}
		for (ServiceItem item : order.getResponse().getServices()) {
			String itemId = new IdModel().create(item.getId()).getId();
			if (Objects.equals(itemId, service.getId())
					|| Objects.equals(itemId, serviceId)) {
				if (item.getSegment() != null
						&& order.getResponse().getSegments() != null) {
					return order.getResponse().getSegments().get(item.getSegment().getId());
				} else {
					return null;
				}
			}
		}
		return null;
	}
	
	private void updateResponse(Order order, List<ServiceItem> services) {
		OrderResponse response = new OrderResponse();
		response.setOrderId(String.valueOf(order.getId()));
		response.setServices(new ArrayList<>());
		
		for (ServiceItem dbItem : order.getResponse().getServices()) {
			for (ServiceItem service : services) {
				if (Objects.equals(dbItem.getId(), service.getId())) {
					
					// данные с ошибками не обновляем
					if (service.getError() == null) {
						
						// обновляем полученные данные в самом OrderResponse, который в базе
						updateServiceData(dbItem, service);
					}
					break;
				}
			}
		}
	}
	
	private void updateServiceData(ServiceItem service, ServiceItem newData) {
		
		// проверяем возможность аннулирования заказа
		if (newData.getTimeToCancel() != null) {
			service.setCanceled(true);
			service.setTimeToCancel(newData.getTimeToCancel());
		} else if (!service.getCanceled()
				&& newData.getCanceled()) {
			service.setCanceled(true);
		}
		if (service.getCanceled()
				&& service.getTimeToCancel() == null) {
			service.setTimeToCancel(new Date(System.currentTimeMillis() + 1200000l));
		}
		if (newData.getExpire() != null) {
			service.setExpire(newData.getExpire());
		}
		if (newData.getReturnConditionId() != null
				&& !newData.getReturnConditionId().isEmpty()) {
			service.setReturnConditionId(newData.getReturnConditionId());
		}
		if (newData.getNumber() != null
				&& !newData.getNumber().isEmpty()) {
			service.setNumber(newData.getNumber());
		}
		if (newData.getAdditionals() != null) {
			if (service.getAdditionals() == null) {
				service.setAdditionals(newData.getAdditionals());
			} else {
				service.getAdditionals().putAll(newData.getAdditionals());
			}
		}
		if (newData.getSeat() != null) {
			service.setSeat(newData.getSeat());
		}
		if (newData.getDocuments() != null) {
			if (service.getDocuments() == null) {
				service.setDocuments(newData.getDocuments());
			} else {
				service.getDocuments().addAll(newData.getDocuments());
			}
		}
	}
	
	/**
	 * Возвращает актуальный статус позиции.
	 */
	public ServiceStatus getLastStatus(Set<ServiceStatusEntity> statuses) {
		return statuses.stream().max(Comparator.comparing(ServiceStatusEntity::getId)).get().getStatus();
	}
	
	/**
	 * Возвращает стоимость указанного статуса или стоимость с предыдущего
	 * статуса, если она отсутствует.
	 */
	public Price getStatusPrice(Set<ServiceStatusEntity> statuses, ServiceStatus status) {
		SortedSet<ServiceStatusEntity> sorted = new TreeSet<>(new Comparator<ServiceStatusEntity>() {

			@Override
			public int compare(ServiceStatusEntity o1, ServiceStatusEntity o2) {
				long diff = o1.getId() - o2.getId();
				if (diff == 0) {
					return 0;
				}
				return diff > 0 ? -1 : 1;
			}
		});
		sorted.addAll(statuses);
		long statusId = 0;
		for (ServiceStatusEntity serviceStatus : sorted) {
			if (serviceStatus.getStatus() == status) {
				if (serviceStatus.getPrice() != null) {
					return serviceStatus.getPrice().getPrice();
				}
				statusId = serviceStatus.getId();
			}
			if (statusId != 0 && statusId > serviceStatus.getId()) {
				if (serviceStatus.getPrice() != null) {
					return serviceStatus.getPrice().getPrice();
				}
			}
		}
		return null;
	}
	
	/**
	 * Удаляет все сервисы с ответа кроме выбранного.
	 */
	public OrderResponse getService(OrderResponse response, long serviceId) {
		for (Iterator<ServiceItem> iterator = response.getServices().iterator(); iterator.hasNext();) {
			ServiceItem service = iterator.next();
			if (!Objects.equals(service.getId(), String.valueOf(serviceId))) {
				iterator.remove();
			}
		}
		updateDictionaries(response);
		return response;
	}
	
	private void updateDictionaries(OrderResponse response) {
		
		// оставляем в запросе только указанный рейс
		if (response.getSegments() != null) {
			response.getSegments().keySet().removeIf(key -> response.getServices() == null
					|| !response.getServices().stream().anyMatch(
							s -> s.getSegment() != null && Objects.equals(key, s.getSegment().getId())));
		}
		// перезаливаем словари
		if (response.getCustomers() != null) {
			response.getCustomers().keySet().removeIf(key -> response.getServices() == null 
					|| !response.getServices().stream().anyMatch(
							s -> s.getCustomer() != null && Objects.equals(key, s.getCustomer().getId())));
		}
		if (response.getVehicles() != null) {
			response.getVehicles().keySet().removeIf(key -> response.getSegments() == null
					|| !response.getSegments().values().stream().anyMatch(
							s -> s.getVehicle() != null && Objects.equals(key, s.getVehicle().getId())));
		}
		if (response.getOrganisations() != null) {
			response.getOrganisations().keySet().removeIf(key -> response.getSegments() == null
					|| !response.getSegments().values().stream().anyMatch(
							s -> (s.getCarrier() != null && Objects.equals(key, s.getCarrier().getId()))
							|| (s.getInsurance() != null && Objects.equals(key, s.getInsurance().getId()))));
		}
		if (response.getLocalities() != null) {
			response.getLocalities().keySet().removeIf(key -> response.getSegments() == null
					|| !response.getSegments().values().stream().anyMatch(
							s -> Objects.equals(key, s.getDeparture().getId())
							|| Objects.equals(key, s.getArrival().getId())));
		}
	}
	
	public Order joinOrders(Order presentOrder, Order newOrder) {
		
		// добавляем новые заказы ресурса в существующий
		for (ResourceOrder resourceOrder : newOrder.getOrders()) {
			presentOrder.addResourceOrder(resourceOrder);
		}
		// обновляем словари
		OrderResponse presentResponse = presentOrder.getResponse();
		OrderResponse newResponse = newOrder.getResponse();
		presentResponse.setSegments(getMap(presentResponse.getSegments(), newResponse.getSegments()));
		presentResponse.setVehicles(getMap(presentResponse.getVehicles(), newResponse.getVehicles()));
		presentResponse.setOrganisations(getMap(presentResponse.getOrganisations(), newResponse.getOrganisations()));
		presentResponse.setLocalities(getMap(presentResponse.getLocalities(), newResponse.getLocalities()));
		presentResponse.setCustomers(getMap(presentResponse.getCustomers(), newResponse.getCustomers()));
		presentResponse.setAdditionals(getMap(presentResponse.getAdditionals(), newResponse.getAdditionals()));
		
		// обновляем сервисы и другое
		presentResponse.setServices(getList(presentResponse.getServices(), newResponse.getServices()));
		presentResponse.setDocuments(getList(presentResponse.getDocuments(), newResponse.getDocuments()));
		return presentOrder;
	}
	
	private <T> List<T> getList(List<T> presentList, List<T> newList) {
		if (newList != null) {
			if (presentList != null) {
				presentList.addAll(newList);
			} else {
				return newList;
			}
		}
		return presentList;
	}
	
	private <T> Map<String, T> getMap(Map<String, T> presentMap, Map<String, T> newMap) {
		if (newMap != null) {
			if (presentMap != null) {
				presentMap.putAll(newMap);
			} else {
				return newMap;
			}
		}
		return presentMap;
	}
	
	public Order removeServices(Order order, List<ServiceItem> removed) {
		Date created = new Date();
		User user = dataController.getUser();
		for (ResourceOrder resourceOrder : order.getOrders()) {
			Set<String> resourceServiceIds = removed.stream().map(ServiceItem::getId).collect(Collectors.toSet());
			if (resourceOrder.getServices().stream().anyMatch(rs -> resourceServiceIds.contains(String.valueOf(rs.getId())))) {
				if (resourceOrder.getServices().stream().allMatch(rs -> resourceServiceIds.contains(String.valueOf(rs.getId())))) {
					for (ResourceService resourceService : resourceOrder.getServices()) {
						
						// добавляем статус об удалении позиции
						resourceService.addStatus(createStatus(created, user, ServiceStatus.REMOVE, null, null));

						// удаляем сервисы из заказа
						for (Iterator<ServiceItem> iterator = order.getResponse().getServices().iterator(); iterator.hasNext();) {
							ServiceItem service = iterator.next();
							if (Objects.equals(service.getId(), resourceService.getResourceNativeServiceId())) {
								iterator.remove();
								break;
							}
						}
					}
				} else {
					throw new RequestValidateException("Services with ids ["
							+ String.join(", ", resourceOrder.getServices().stream().map(rs -> String.valueOf(rs.getId())).collect(Collectors.toSet()))
							+ "] must be removed together");
				}
			}
		}
		updateDictionaries(order.getResponse());
		return order;
	}
	
	/**
	 * Добавляет документы в заказ из бд.
	 */
	public Order addDocuments(Order order, List<OrderResponse> responses) {
		for (OrderResponse response : responses) {
			addDocuments(order, response.getDocuments(), null);
			if (response.getServices() != null) {
				for (ServiceItem service : response.getServices()) {
					addDocuments(order, service.getDocuments(), service);
				}
			}
		}
		return order;
	}
	
	private void addDocuments(Order order, List<Document> documents, ServiceItem service) {
		if (documents != null) {
			for (Document document : documents) {
				OrderDocument orderDocument = new OrderDocument();
				orderDocument.setType(document.getType() == null ? DocumentType.TICKET : document.getType());
				orderDocument.setBase64(document.getBase64());
				
				// ищем ид сервиса
				if (service != null) {
					out:
						for (ResourceOrder resourceOrder : order.getOrders()) {
							for (ResourceService resourceService : resourceOrder.getServices()) {
								if (Objects.equals(service.getId(),
										new IdModel().create(resourceService.getResourceNativeServiceId()).getId())) {
									orderDocument.setServiceId(resourceService.getId());
									break out;
								}
							}
						}
				}
				order.addOrderDocument(orderDocument);
			}
		}
	}
	
	/**
	 * Формирует ответ с документами заказа.
	 */
	public OrderResponse getDocumentsResponse(Order order) {
		OrderResponse response = new OrderResponse();
		response.setOrderId(String.valueOf(order.getId()));
		Map<Long, ServiceItem> services = new HashMap<>();
		for (OrderDocument document : order.getDocuments()) {
			Document serviceDocument = new Document(document.getType(), document.getBase64());
			if (document.getServiceId() != 0) {
				ServiceItem service = services.get(document.getServiceId());
				if (service == null) {
					service = new ServiceItem();
					service.setId(String.valueOf(document.getServiceId()));
					service.setDocuments(new ArrayList<>());
					services.put(document.getServiceId(), service);
				}
				service.getDocuments().add(serviceDocument);
			} else {
				if (response.getDocuments() == null) {
					response.setDocuments(new ArrayList<>());
				}
				response.getDocuments().add(serviceDocument);
			}
		}
		if (response.getDocuments() != null
				&& response.getDocuments().isEmpty()) {
			response.setDocuments(null);
		}
		return response;
	}
	
	/**
	 * Проставляет стоимость статусам, у которых ее нет
	 */
	public List<Order> addPrice(List<Order> orders) {
		for (Order order : orders) {
			for (ResourceOrder resourceOrder : order.getOrders()) {
				for (ResourceService resourceService : resourceOrder.getServices()) {
					Set<ServiceStatusEntity> statuses = new HashSet<>(resourceService.getStatuses());
					for (ServiceStatusEntity serviceStatus : statuses) {
						if (serviceStatus.getPrice() == null) {
							serviceStatus.setPrice(getStatusPrice(statuses, serviceStatus.getStatus()));
						}
					}
				}
			}
		}
		return orders;
	}

}
