package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderDocument;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.Document;
import com.gillsoft.model.DocumentType;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Price;
import com.gillsoft.model.Resource;
import com.gillsoft.model.RestError;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.Trip;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.User;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderResponseConverter {
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private LocalityController localityController;
	
	@Autowired
	private TripSearchController searchController;
	
	@Autowired
	private DiscountController discountController;
	
	public Order convertToNewOrder(OrderResponse response) {
		OrderRequest originalRequest = simulateOriginalRequest(response);
		OrderRequest createRequest = simulateCreateRequest(response);
		return convertToNewOrder(originalRequest, createRequest, new OrderResponse(), response);
	}
	
	private OrderRequest simulateOriginalRequest(OrderResponse response) {
		response.setId(StringUtil.generateUUID());
		OrderRequest originalRequest = new OrderRequest();
		originalRequest.setId(response.getId());
		originalRequest.setCustomers(response.getCustomers());
		originalRequest.setServices(new ArrayList<>(0));
		originalRequest.setOrderId(response.getOrderId());
		return originalRequest;
	}
	
	private OrderRequest simulateCreateRequest(OrderResponse response) {
		OrderRequest createRequest = new OrderRequest();
		createRequest.setResources(new ArrayList<>(response.getResources().size()));
		for (OrderResponse orderResponse : response.getResources()) {
			orderResponse.setId(StringUtil.generateUUID());
			if (orderResponse.getSegments() != null
					&& !orderResponse.getSegments().isEmpty()) {
				OrderRequest resourceRequest = new OrderRequest();
				resourceRequest.setId(orderResponse.getId());
				resourceRequest.setCustomers(response.getCustomers());
				resourceRequest.setServices(new ArrayList<>(0));
				resourceRequest.setCurrency(orderResponse.getServices().get(0).getPrice().getCurrency());
				Resource resource = orderResponse.getSegments().values().iterator().next().getResource();
				resourceRequest.setParams(createResourceParams(resource));
				createRequest.getResources().add(resourceRequest);
			}
		}
		return createRequest;
	}
	
	private ResourceParams createResourceParams(Resource resource) {
		ResourceParams resourceParams = new ResourceParams();
		Resource copy = new Resource();
		resourceParams.setResource(copy);
		com.gillsoft.ms.entity.ResourceParams orderParams = getResourceParam(resource.getId());
		if (orderParams != null) {
			resourceParams.setId(String.valueOf(orderParams.getId()));
			copy.setId(String.valueOf(getParamsResourceId(orderParams)));
		} else {
			copy.setId(resource.getId());
		}
		return resourceParams;
	}
	
	private com.gillsoft.ms.entity.ResourceParams getResourceParam(String id) {
		try {
			return dataController.getResourceParam(Long.parseLong(id));
		} catch (Exception e) {
			return null;
		}
	}
	
	private long getParamsResourceId(com.gillsoft.ms.entity.ResourceParams params) {
		com.gillsoft.ms.entity.Resource resourceOrders = dataController.getResource(params);
		if (resourceOrders != null) {
			return resourceOrders.getId();
		}
		return -1;
	}
	
	public Order convertToNewOrder(OrderRequest originalRequest, OrderRequest createRequest, OrderResponse result,
			OrderResponse response) {
		
		result.setId(originalRequest.getId());
		result.setCustomers(originalRequest.getCustomers());
		result.setServices(new ArrayList<>());
		
		// заказ для сохранения
		Date created = new Date();
		Order order = new Order();
		order.setCreated(created);
		
		// по умолчанию время на выкуп 20 минут
		Date expire = new Date(System.currentTimeMillis() + 1200000l);
		order.setResponse(result);
		
		User user = dataController.getUser();
		
		Set<String> resultSegmentIds = new HashSet<>(); // ид рейсов в ответе
		Map<String, String> segmentIds = getSegmentIds(originalRequest);
		
		// преобразовываем ответ
		for (OrderResponse orderResponse : response.getResources()) {
			Optional<OrderRequest> optional = createRequest.getResources().stream().filter(r -> r.getId().equals(orderResponse.getId())).findFirst();
			if (optional.isPresent()) {
				
				// запрос, по которому получен результат
				OrderRequest currRequest = optional.get();
				
				// заказы ресурсов для сохранения
				ResourceOrder resourceOrder = createResourceOrder(currRequest, orderResponse);
				order.addResourceOrder(resourceOrder);
				if (orderResponse.getError() != null) {
					
					// маппим рейсы заказа из запроса так как в ответе ошибка и их нет
					orderResponse.setSegments(currRequest.getServices().stream().map(
							s -> s.getSegment() != null ? s.getSegment().getId() : null).filter(v -> v != null).collect(
									Collectors.toMap(v -> v, v -> new Segment(), (v1, v2) -> v1)));
					searchController.mapScheduleSegment(new ArrayList<>(segmentIds.keySet()), currRequest, orderResponse, result);
					for (ServiceItem item : currRequest.getServices()) {
						
						// устанавливаем ид сегмента рейса
						if (item.getSegment() != null) {
							if (result.getSegments() != null) {
								setSegment(result.getSegments(), item);
							}
							resultSegmentIds.add(item.getSegment().getId());
						}
					}
					currRequest.getServices().forEach(s -> s.setError(orderResponse.getError()));
					result.getServices().addAll(currRequest.getServices());
					
					// сервисы ресурса для сохранения
					currRequest.getServices().forEach(s -> {
						resourceOrder.addResourceService(createResourceService(created, user, resourceOrder.getId(), s, ServiceStatus.NEW_ERROR,
								orderResponse.getError().getMessage()));
					});
				} else {
					// маппим рейсы заказа из ответа
					searchController.mapScheduleSegment(new ArrayList<>(segmentIds.keySet()), currRequest, orderResponse, result);
					
					for (ServiceItem item : orderResponse.getServices()) {
						
						// устанавливаем ид сегмента рейса
						Segment segment = null;
						if (item.getSegment() != null) {
							if (result.getSegments() != null) {
								setSegment(result.getSegments(), item);
								segment = result.getSegments().get(item.getSegment().getId());
							}
							resultSegmentIds.add(item.getSegment().getId());
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
							// добавляем условия возврата
							addReturnConditions(item, segment);
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
		// очищаем неиспользуемые данные из словарей
		searchController.updateResponseDictionaries(resultSegmentIds, result.getSegments(), result.getVehicles(), result.getOrganisations(), result.getLocalities(), null);
		
		// ид сегментов должны быть с next
		updateResultSegmentIds(result, segmentIds);
		
		// проверяем наличие стыковки по каждому заказчику и насчитываем скидку
		applyConnectionDiscount(result, order);
		
		// сбрасываем стоимости с рейсов - они в сервисах
		result.getSegments().values().forEach(s -> s.setPrice(null));
		
		return order;
	}
	
	private ResourceOrder createResourceOrder(OrderRequest request, OrderResponse response) {
		ResourceOrder resourceOrder = new ResourceOrder();
		resourceOrder.setResourceId(Long.parseLong(request.getParams().getResource().getId()));
		try {
			resourceOrder.setResourceParamId(Long.parseLong(request.getParams().getId()));
		} catch (Exception e) {
		}
		resourceOrder.setResourceNativeOrderId(
				new IdModel(Long.parseLong(request.getParams().getResource().getId()), response.getOrderId()).asString());
		return resourceOrder;
	}
	
	private void applyConnectionDiscount(OrderResponse result, Order order) {
		for (String customerId : result.getCustomers().keySet()) {
			List<ServiceItem> services = result.getServices().stream()
					.filter(s -> s.getError() == null && s.getCustomer() != null && customerId.equals(s.getCustomer().getId())).collect(Collectors.toList());
			for (Trip trip : getTrips(services)) {
				
				// проставляем стоимость на сегменты с текущих сервисов, чтобы ее пересчитали (сервисов из ResourceService)
				for (ServiceItem service : services) {
					if (service.getSegment() != null) {
						out:
							for (ResourceOrder resourceOrder : order.getOrders()) {
								for (ResourceService resourceService : resourceOrder.getServices()) {
									if (isServiceOfResourceService(service, resourceService)) {
										result.getSegments().get(service.getSegment().getId()).setPrice(
												resourceService.getStatuses().iterator().next().getPrice().getPrice());
										break out;
									}
								}
							}
					}
				}
				discountController.applyConnectionDiscount(trip, result.getSegments());
				
				// обновляем стоимости в сервисах
				for (ServiceItem service : services) {
					if (service.getSegment() != null) {
						out:
							for (ResourceOrder resourceOrder : order.getOrders()) {
								for (ResourceService resourceService : resourceOrder.getServices()) {
									if (isServiceOfResourceService(service, resourceService)) {
										resourceService.getStatuses().iterator().next().getPrice().setPrice(
												result.getSegments().get(service.getSegment().getId()).getPrice());
										break out;
									}
								}
							}
					}
				}
			}
		}
	}
	
	private void updateResultSegmentIds(OrderResponse result, Map<String, String> segmentIds) {
		if (result.getSegments() != null) {
			for (Entry<String, String> entry : segmentIds.entrySet()) {
				if (result.getSegments().containsKey(entry.getKey())) {
					Segment segment = result.getSegments().get(entry.getKey());
					result.getSegments().remove(entry.getKey());
					result.getSegments().put(entry.getValue(), segment);
					for (ServiceItem service : result.getServices()) {
						if (service.getSegment() != null
								&& entry.getKey().equals(service.getSegment().getId())) {
							service.getSegment().setId(entry.getValue());
						}
					}
				}
			}
		}
	}
	
	private List<Trip> getTrips(List<ServiceItem> services) {
		List<Trip> trips = new ArrayList<>();
		List<String> ids = services.stream().filter(s -> s.getSegment() != null)
				.map(s -> s.getSegment().getId()).collect(Collectors.toList());
		for (int i = 0; i < ids.size(); i++) {
			String tripId = ids.get(i);
			TripIdModel idModel = new TripIdModel().create(tripId);
			if (idModel.getNext() != null) {
				Set<String> next = idModel.getNext();
				idModel.setNext(null);
				for (String nextId : next) {
					
					// nextId в md5
					// ищем его в имеющемся результате
					nextId = getSegmentId(nextId, ids);
					if (nextId != null) {
						boolean added = false;
						for (Trip trip : trips) {
							if (trip.getSegments() == null) {
								trip.setSegments(new ArrayList<>());
							} else {
								if (trip.getSegments().get(0).equals(nextId)) {
									trip.getSegments().add(0, tripId);
								}
								if (trip.getSegments().get(trip.getSegments().size() - 1).equals(tripId)) {
									trip.getSegments().add(nextId);
								}
							}
						}
						if (!added) {
							Trip trip = new Trip();
							trip.setSegments(new ArrayList<>());
							trip.getSegments().add(tripId);
							trip.getSegments().add(nextId);
							trips.add(trip);
						}
					}
				}
			}
		}
		return trips;
	}
	
	private String getSegmentId(String md5Id, List<String> ids) {
		for (String id : ids) {
			TripIdModel idModel = new TripIdModel().create(id);
			idModel.setNext(null);
			if (md5Id.equals(StringUtil.md5(idModel.asString()))) {
				return id;
			}
		}
		return null;
	}
	
	private Map<String, String> getSegmentIds(OrderRequest request) {
		return request.getServices().stream().filter(s -> s.getSegment() != null)
				.collect(Collectors.toMap(s -> {
					TripIdModel id = new TripIdModel().create(s.getSegment().getId());
					id.setNext(null);
					return id.asString();
				}, s -> s.getSegment().getId(), (s1, s2) -> s1));
	}
	
	private void addReturnConditions(ServiceItem item, Segment segment) {
		
		// проверяем условия возврата и, если нет, то берем с рейса
		if (item.getPrice() != null
				&& item.getPrice().getTariff() != null
				&& (item.getPrice().getTariff().getReturnConditions() == null
					|| item.getPrice().getTariff().getReturnConditions().isEmpty())
				&& segment != null
				&& segment.getPrice() != null
				&& segment.getPrice().getTariff() != null) {
			item.getPrice().getTariff().setReturnConditions(segment.getPrice().getTariff().getReturnConditions());
		}
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
		if (service.getPrice() != null) {
			service.setPrice(service.getPrice().getSource());
		}
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
		response.setPayment(order.getPayment());
		response.setCancelReason(order.getCancelReason());
		for (ServiceItem service : response.getServices()) {
			if (service.getExpire() != null) {
				service.setExpire(convertUTCDateToUserTimeZone(service.getExpire()));
			}
			if (service.getTimeToCancel() != null) {
				if (service.getTimeToCancel().getTime() < System.currentTimeMillis() + 60000) {
					service.setCanceled(false);
					service.setTimeToCancel(null);
				} else {
					service.setTimeToCancel(convertUTCDateToUserTimeZone(service.getTimeToCancel()));
				}
			}
			if (service.getId() != null) {
				out:
					for (ResourceOrder resourceOrder : order.getOrders()) {
						for (ResourceService resourceService : resourceOrder.getServices()) {
							if (isServiceOfResourceService(service, resourceService)) {
								
								// данные пользователя обновившего сервис
								ServiceStatusEntity statusEntity = getLastNotErrorStatusEntity(resourceService.getStatuses());
								if (statusEntity == null) {
									statusEntity = getLastStatusEntity(resourceService.getStatuses());
								}
								service.setUpdated(convertUTCDateToUserTimeZone(statusEntity.getCreated()));
								User user = dataController.getUser(statusEntity.getUserId());
								if (user != null) {
									com.gillsoft.model.User modelUser = DataConverter.convert(user);
									service.setUpdateUser(modelUser);
									Organisation organisation = dataController.getOrganisation(statusEntity.getOrganisationId());
									if (organisation != null) {
										modelUser.setOrganisation(DataConverter.convert(organisation));
									}
								}
								// данные пользователя создавшего сервис
								ServiceStatusEntity createdEntity = getStatusEntity(ServiceStatus.NEW, resourceService.getStatuses());
								if (createdEntity == null) {
									createdEntity = statusEntity;
								}
								service.setCreated(convertUTCDateToUserTimeZone(createdEntity.getCreated()));
								user = dataController.getUser(createdEntity.getUserId());
								if (user != null) {
									com.gillsoft.model.User modelUser = DataConverter.convert(user);
									service.setCreateUser(modelUser);
									Organisation organisation = dataController.getOrganisation(createdEntity.getOrganisationId());
									if (organisation != null) {
										modelUser.setOrganisation(DataConverter.convert(organisation));
									}
								}
								service.setId(String.valueOf(resourceService.getId()));
								ServiceStatus status = getLastStatus(resourceService.getStatuses());
								service.setStatus(status.name());
								
								// устанавливаем стоимость с указанного статуса
								Price price = getStatusPrice(resourceService.getStatuses(), status);
								if (price != null) {
									price.setSource(service.getPrice());
									service.setPrice(price);
								}
								break out;
							}
						}
					}
			}
		}
		response.fillMaps();
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
			Optional<OrderRequest> optional = requests.stream().filter(r -> r.getId().equals(orderResponse.getId())).findFirst();
			if (optional != null) {
				
				// запрос, по которому получен результат
				OrderRequest currRequest = optional.get();
				
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
									resourceService.addStatus(createStatus(created, user, errorStatus, orderResponse.getError().getMessage(), null));
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
									if (isServiceOfResourceService(service, resourceService)) {
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
													service.getError() == null ? null : service.getError().getMessage(),
													service.getPrice()));
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
	
	public Order convertToConfirm(Order order, List<OrderResponse> responses,
			ServiceStatus confirmStatus, ServiceStatus errorStatus) {
		List<OrderRequest> requests = createConfirmRequests(responses);
		return convertToConfirm(order, requests, responses, confirmStatus, errorStatus);
	}
	
	private List<OrderRequest> createConfirmRequests(List<OrderResponse> responses) {
		return responses.stream().map(r -> simulateOriginalRequest(r)).collect(Collectors.toList());
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
		updateResponse(order, responses, joinServices(order, requests, responses, confirmStatus, errorStatus));
		return order;
	}
	
	public OrderResponse convertToReturnCalc(Order order, List<OrderRequest> requests, List<OrderResponse> responses) {
		OrderResponse response = new OrderResponse();
		response.setServices(joinServices(order, requests, responses, null, null));
		response = convertResponse(order, response);
		for (ServiceItem service : response.getServices()) {
			if (service.getError() == null) {
				Segment segment = getSegment(order, service);
				service.setPrice(dataController.recalculateReturn(segment,
						getDepartureTimeZone(segment), service.getPrice(), service.getPrice().getSource()));
			}
		}
		return response;
	}
	
	public Order convertToReturn(Order order, List<OrderResponse> returnResponses) {
		List<OrderRequest> requests = createConfirmRequests(returnResponses);
		return convertToReturn(order, requests, returnResponses, null);
	}
	
	public Order convertToReturn(Order order, List<OrderRequest> requests, List<OrderResponse> returnResponses, List<OrderResponse> calcResponses) {

		// проверяем стоимости возвратов
		for (OrderResponse orderResponse : returnResponses) {
			if (orderResponse.getServices() != null) {
				for (ServiceItem service : orderResponse.getServices()) {
					if (service.getError() == null) {
						Price price = service.getPrice();
						if (price == null
								&& calcResponses != null) {
							for (OrderResponse calcResponse : calcResponses) {
								if (calcResponse.getServices() != null) {
									Optional<ServiceItem> finded = calcResponse.getServices().stream().filter(s -> Objects.equals(s.getId(), service.getId())).findFirst();
									if (finded.isPresent()) {
										service.setPrice(finded.get().getPrice());
										break;
									}
								}
							}
						}
					}
				}
			}
		}
		// устанавливаем суммы возвратов
		convertToReturnCalc(order, requests, returnResponses);
		
		// добавляем статусы возврата к заказу
		joinServices(order, requests, returnResponses, ServiceStatus.RETURN, ServiceStatus.RETURN_ERROR);
		return order;
	}
	
	/*
	 * Возвращает сегмент рейса, по переданному сервису.
	 */
	private Segment getSegment(Order order, ServiceItem service) {
		ServiceItem orderService = getOrderService(order, service);
		if (orderService != null
				&& orderService.getSegment() != null
				&& order.getResponse().getSegments() != null) {
			return order.getResponse().getSegments().get(orderService.getSegment().getId());
		}
		return null;
	}
	
	private String getDepartureTimeZone(Segment segment) {
		if (segment != null) {
			return Utils.getLocalityTimeZone(segment.getDeparture().getId());
		}
		return null;
	}
	
	private ServiceItem getOrderService(Order order, ServiceItem service) {
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				if (isServiceOfResourceService(service, resourceService)) {
					for (ServiceItem item : order.getResponse().getServices()) {
						if (isServiceOfResourceService(item, resourceService)) {
							return item;
						}
					}
				}
			}
		}
		return null;
	}
	
	private void updateResponse(Order order, List<OrderResponse> responses, List<ServiceItem> services) {
		updateOrderData(order, responses);
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
	
	private void updateOrderData(Order order, List<OrderResponse> responses) {
		for (OrderResponse orderResponse : responses) {
			for (ResourceOrder resourceOrder : order.getOrders()) {
				IdModel idModel = new IdModel().create(resourceOrder.getResourceNativeOrderId());
				if (Objects.equals(orderResponse.getOrderId(), idModel.getId())) {
					if (orderResponse.getNewOrderId() != null
							&& !orderResponse.getNewOrderId().isEmpty()) {
						idModel.setId(orderResponse.getNewOrderId());
						resourceOrder.setResourceNativeOrderId(idModel.asString());
					}
					for (ServiceItem service : orderResponse.getServices()) {
						for (ResourceService resourceService : resourceOrder.getServices()) {
							if (isServiceOfResourceService(service, resourceService)
									&& service.getNewId() != null
									&& !service.getNewId().isEmpty()) {
								IdModel serviceIdModel = new IdModel().create(resourceService.getResourceNativeServiceId());
								serviceIdModel.setId(service.getNewId());
								resourceService.setResourceNativeServiceId(serviceIdModel.asString());
							}
						}
					}
				}
			}
		}
	}
	
	private void updateServiceData(ServiceItem service, ServiceItem newData) {
		if (newData.getNewId() != null
				&& !newData.getNewId().isEmpty()) {
			service.setId(newData.getNewId());
		}
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
		return getLastStatusEntity(statuses).getStatus();
	}
	
	public ServiceStatus getLastNotErrorStatus(Set<ServiceStatusEntity> statuses) {
		return getLastNotErrorStatusEntity(statuses).getStatus();
	}
	
	public ServiceStatusEntity getStatusEntity(ServiceStatus status, Set<ServiceStatusEntity> statuses) {
		Optional<ServiceStatusEntity> statusEntity = statuses.stream().filter(s -> s.getStatus() == status).findFirst();
		return statusEntity.isPresent() ? statusEntity.get() : null;
	}
	
	public ServiceStatusEntity getLastStatusEntity(Set<ServiceStatusEntity> statuses) {
		Optional<ServiceStatusEntity> statusEntity = statuses.stream().max(Comparator.comparing(ServiceStatusEntity::getId));
		return statusEntity.isPresent() ? statusEntity.get() : null;
	}
	
	public ServiceStatusEntity getLastNotErrorStatusEntity(Set<ServiceStatusEntity> statuses) {
		
		// удаляем статусы с ошибками
		Set<ServiceStatusEntity> temp = new HashSet<>(statuses);
		temp.removeIf(s -> s.getStatus().name().contains("ERROR"));
		
		return getLastStatusEntity(temp);
	}
	
	/**
	 * Возвращает стоимость указанного статуса или стоимость с предыдущего
	 * статуса, если она отсутствует.
	 */
	public Price getStatusPrice(Set<ServiceStatusEntity> statuses, ServiceStatus status) {
		List<ServiceStatusEntity> sorted = new ArrayList<>(statuses);
		sorted.sort((s1, s2) -> s1.getId() > s2.getId() ? -1 : 1);
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
							if (isServiceOfResourceService(service, resourceService)) {
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
	public Order addDocuments(Order order, List<OrderRequest> requests, List<OrderResponse> responses) {
		for (OrderResponse response : responses) {
			addDocuments(order, getMaxStatus(order, requests, responses), response.getDocuments(), null);
			if (response.getServices() != null) {
				for (ServiceItem service : response.getServices()) {
					addDocuments(order, null, service.getDocuments(), service);
				}
			}
		}
		return order;
	}
	
	// максимально последнмй статус любой позиции заказа ресурса
	private ServiceStatus getMaxStatus(Order order, List<OrderRequest> requests, List<OrderResponse> responses) {
		for (OrderRequest request : requests) {
			if (responses.stream().filter(resp -> Objects.equals(request.getId(), resp.getId())).findFirst().isPresent()) {
				for (ResourceOrder resourceOrder : order.getOrders()) {
					if (Objects.equals(request.getOrderId(), new IdModel().create(resourceOrder.getResourceNativeOrderId()).getId())) {
						ServiceStatusEntity maxStatus = null;
						for (ResourceService resourceService : resourceOrder.getServices()) {
							ServiceStatusEntity status = getLastNotErrorStatusEntity(resourceService.getStatuses());
							if (maxStatus == null
									|| maxStatus.getCreated().getTime() < status.getCreated().getTime()) {
								maxStatus = status;
							}
						}
						return maxStatus.getStatus();
					}
				}
			}
		}
		return null;
	}
	
	// максимально последнмй статус любой позиции заказа
	private ServiceStatus getMaxStatus(Order order) {
		ServiceStatusEntity maxStatus = null;
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				ServiceStatusEntity status = getLastNotErrorStatusEntity(resourceService.getStatuses());
				if (maxStatus == null
						|| maxStatus.getCreated().getTime() < status.getCreated().getTime()) {
					maxStatus = status;
				}
			}
		}
		return maxStatus.getStatus();
	}
	
	public void addDocuments(Order order, ServiceStatus orderStatus, List<Document> documents, ServiceItem service) {
		if (documents != null) {
			for (Document document : documents) {
				OrderDocument orderDocument = new OrderDocument();
				orderDocument.setType(document.getType() == null ? DocumentType.TICKET : document.getType());
				orderDocument.setBase64(document.getBase64());
				orderDocument.setStatus(orderStatus);
				
				// ищем ид сервиса
				if (service != null
						&& service.getError() == null) {
					out:
						for (ResourceOrder resourceOrder : order.getOrders()) {
							for (ResourceService resourceService : resourceOrder.getServices()) {
								if (isServiceOfResourceService(service, resourceService)) {
									orderDocument.setServiceId(resourceService.getId());
									orderDocument.setStatus(getLastNotErrorStatus(resourceService.getStatuses()));
									break out;
								}
							}
						}
				}
				order.addOrderDocument(orderDocument);
			}
		}
	}
	
	public boolean isServiceOfResourceService(ServiceItem service, ResourceService resourceService) {
		return Objects.equals(service.getId(), resourceService.getResourceNativeServiceId())
				|| Objects.equals(service.getId(), new IdModel().create(resourceService.getResourceNativeServiceId()).getId())
				|| Objects.equals(service.getId(), String.valueOf(resourceService.getId()));
	}
	
	/**
	 * Формирует ответ с документами заказа.
	 */
	public OrderResponse getDocumentsResponse(Order order) {
		OrderResponse response = new OrderResponse();
		response.setOrderId(String.valueOf(order.getId()));
		Map<Long, ServiceItem> services = new HashMap<>();
		
		// максимально последнмй статус любой позиции заказа
		ServiceStatus maxStatus = getMaxStatus(order);
		for (OrderDocument document : order.getDocuments()) {
			
			// проверяем статус позиции текущего документа
			// если документ для всего заказа, то проверяется maxStatus
			if (isAddToResponse(order, document, maxStatus)) {
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
		}
		if (response.getDocuments() != null
				&& response.getDocuments().isEmpty()) {
			response.setDocuments(null);
		}
		if (!services.isEmpty()) {
			response.setServices(new ArrayList<>(services.values()));
		}
		return response;
	}
	
	private boolean isAddToResponse(Order order, OrderDocument document, ServiceStatus status) {
		if (document.getServiceId() != 0) {
			for (ResourceOrder resourceOrder : order.getOrders()) {
				for (ResourceService resourceService : resourceOrder.getServices()) {
					if (document.getId() == resourceService.getId()) {
						return getLastNotErrorStatus(resourceService.getStatuses()) == document.getStatus();
					}
				}
			}
		}
		return status == document.getStatus();
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
					// меняем статусы отмены
					List<ServiceStatusEntity> sorted = new ArrayList<>(statuses);
					sorted.sort((s1, s2) -> s1.getId() > s2.getId() ? -1 : 1);
					for (int i = 0; i < sorted.size(); i++) {
						ServiceStatusEntity statusEntity = sorted.get(i);
						if (statusEntity.getStatus() == ServiceStatus.CANCEL) {
							for (int j = i + 1; j < sorted.size(); j++) {
								ServiceStatusEntity prevStatusEntity = sorted.get(j);
								try {
									ServiceStatus newStatus = ServiceStatus.valueOf(ServiceStatus.CANCEL + "_" + prevStatusEntity.getStatus());
									if (newStatus != null) {
										statusEntity.setStatus(newStatus);
										break;
									}
								} catch (Exception e) {
								}
							}
							break;
						}
					}
				}
			}
		}
		return orders;
	}
	
	/**
	 * Заполняем сегменты данными из словарей.
	 * 
	 * @param response
	 *            Заказ.
	 */
	public void updateSegments(OrderResponse response) {
		if (response.getLocalities() != null) {
			for (Entry<String, Locality> entry : response.getLocalities().entrySet()) {
				Locality locality = entry.getValue();
				locality.setId(entry.getKey());
				if (locality.getParent() != null
						&& locality.getParent().getId() != null) {
					if (response.getLocalities().containsKey(locality.getParent().getId())) {
						locality.setParent(response.getLocalities().get(locality.getParent().getId()));
					} else {
						try {
							Locality parent = localityController.getLocality(Long.valueOf(locality.getParent().getId()));
							if (parent != null) {
								locality.setParent(parent);
							}
						} catch (NumberFormatException e) {
						}
					}
				}
			}
		}
		if (response.getSegments() != null) {
			for (Entry<String, Segment> entry : response.getSegments().entrySet()) {
				Segment segment = entry.getValue();
				segment.setId(entry.getKey());
				segment.setDeparture(response.getLocalities().get(segment.getDeparture().getId()));
				segment.setArrival(response.getLocalities().get(segment.getArrival().getId()));
				if (response.getSegments() != null) {
					if (segment.getCarrier() != null) {
						segment.setCarrier(response.getOrganisations().get(segment.getCarrier().getId()));
					}
					if (segment.getInsurance() != null) {
						segment.setInsurance(response.getOrganisations().get(segment.getInsurance().getId()));
					}
				}
			}
		}
		if (response.getCustomers() != null) {
			response.getCustomers().forEach((k, v) -> v.setId(k));
		}
		for (ServiceItem service : response.getServices()) {
			if (service.getSegment() != null) {
				service.setSegment(response.getSegments().get(service.getSegment().getId()));
			}
			if (service.getCustomer() != null) {
				service.setCustomer(response.getCustomers().get(service.getCustomer().getId()));
			}
			if (response.getUsers() != null) {
				if (service.getCreateUser() != null) {
					service.setCreateUser(response.getUsers().get(service.getCreateUser().getId()));
					if (service.getCreateUser().getOrganisation() != null) {
						service.getCreateUser().setOrganisation(response.getOrganisations().get(
								service.getCreateUser().getOrganisation().getId()));
					}
				}
				if (service.getUpdateUser() != null) {
					service.setUpdateUser(response.getUsers().get(service.getUpdateUser().getId()));
					if (service.getUpdateUser().getOrganisation() != null) {
						service.getUpdateUser().setOrganisation(response.getOrganisations().get(
								service.getUpdateUser().getOrganisation().getId()));
					}
				}
			}
		}
	}
	
	public void checkDiscountForReturn(Order order, OrderRequest request) {
		
		// список сервисов из запроса на возврат
		Set<String> requestItems = request.getServices().stream().map(ServiceItem::getId).collect(Collectors.toSet());
		
		OrderResponse response = getResponse(order);
		
		// список сервисов сгруппированных по заказчику
		Map<String, List<ServiceItem>> customersItems = response.getServices().stream().collect(
				Collectors.groupingBy(s -> s.getCustomer().getId()));
		
		StringBuilder errorMsg = new StringBuilder();
		for (Entry<String, List<ServiceItem>> customerItems : customersItems.entrySet()) {
			Set<String> requestCustomerItems = customerItems.getValue().stream()
					.filter(s -> requestItems.contains(s.getId())).map(ServiceItem::getId).collect(Collectors.toSet());
			if (!requestCustomerItems.isEmpty()) {
				Set<String> idsWithDiscounts = new HashSet<>();
				for (ServiceItem service : customerItems.getValue()) {
					if (service.getError() == null
							&& service.getPrice() != null
							&& service.getPrice().getDiscounts() != null) {
						idsWithDiscounts.add(service.getId());
					}
				}
				if (!idsWithDiscounts.isEmpty()) {
					Set<String> customerItemIds = customerItems.getValue().stream().map(ServiceItem::getId).collect(Collectors.toSet());
					
					// если список сервисов на возврат не содержит всех скидочных сервисов или всех сервисов пользователя
					if (!requestCustomerItems.containsAll(idsWithDiscounts)
							|| !requestCustomerItems.containsAll(customerItemIds)) {
						errorMsg.append("You can return services only in groupe ").append(String.join(";", idsWithDiscounts));
						if (!idsWithDiscounts.containsAll(customerItemIds)) {
							errorMsg.append(" or ").append(String.join(";", customerItemIds));
						}
						errorMsg.append("\r\n");
					}
				}
			}
		}
		if (errorMsg.length() > 0) {
			throw new MethodUnavalaibleException(errorMsg.toString().trim());
		}
	}

}
