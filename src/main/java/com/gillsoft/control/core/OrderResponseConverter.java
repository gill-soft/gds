package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.RequestValidateException;
import com.gillsoft.control.core.data.DataConverter;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.OrderClient;
import com.gillsoft.control.service.model.OrderDocument;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.Document;
import com.gillsoft.model.DocumentType;
import com.gillsoft.model.Price;
import com.gillsoft.model.Resource;
import com.gillsoft.model.RestError;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.User;
import com.gillsoft.util.ContextProvider;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderResponseConverter {
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ClientController clientController;
	
	public OrderRequest simulateOriginalRequest(OrderResponse response) {
		response.setId(StringUtil.generateUUID());
		OrderRequest originalRequest = new OrderRequest();
		originalRequest.setId(response.getId());
		originalRequest.setCustomers(response.getCustomers());
		originalRequest.setServices(new ArrayList<>(0));
		originalRequest.setOrderId(response.getOrderId());
		return originalRequest;
	}
	
	public ResourceParams createResourceParams(Resource resource) {
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
	
	public com.gillsoft.ms.entity.ResourceParams getResourceParam(String id) {
		try {
			return dataController.getResourceParam(Long.parseLong(id));
		} catch (Exception e) {
			return null;
		}
	}
	
	public long getParamsResourceId(com.gillsoft.ms.entity.ResourceParams params) {
		com.gillsoft.ms.entity.Resource resourceOrders = dataController.getResource(params);
		if (resourceOrders != null) {
			return resourceOrders.getId();
		}
		return -1;
	}
	
	public Order convertToNewOrder(OrderRequest originalRequest, OrderRequest resuorcesRequests, OrderResponse result,
			OrderResponse resourcesResponses) {
		
		OrderResponseHelper orderResponseHelper = ContextProvider.getBean(OrderResponseHelper.class);
		result.setId(originalRequest.getId());
		result.setCustomers(originalRequest.getCustomers());
		result.setServices(new ArrayList<>());
		
		// заказ для сохранения
		Date created = new Date();
		Order order = new Order();
		order.setCreated(created);
		
		clientController.addClientsToOrder(order, originalRequest.getCustomers());
		
		// по умолчанию время на выкуп 20 минут
		Date expire = new Date(System.currentTimeMillis() + 1200000l);
		order.setResponse(result);
		
		User user = dataController.getUser();
		
		// подготавливаем данные перед обработкой ответов
		orderResponseHelper.beforeOrder(originalRequest, result, resuorcesRequests, resourcesResponses);
		
		// преобразовываем ответ
		for (OrderResponse orderResponse : resourcesResponses.getResources()) {
			Optional<OrderRequest> optional = resuorcesRequests.getResources().stream().filter(r -> r.getId().equals(orderResponse.getId())).findFirst();
			if (optional.isPresent()) {
				orderResponse.fillMaps();
				
				// запрос, по которому получен результат
				OrderRequest currRequest = optional.get();
				
				// подготавливаем данные перед обработкой сервисов
				orderResponseHelper.beforeServices(result, currRequest, orderResponse);

				// заказы ресурсов для сохранения
				ResourceOrder resourceOrder = createResourceOrder(currRequest, orderResponse);
				order.addResourceOrder(resourceOrder);
				if (orderResponse.getError() != null) {
					for (ServiceItem item : currRequest.getServices()) {
						item.setError(orderResponse.getError());
						
						// подготавливаем данные во время начала обработки сервиса
						orderResponseHelper.beforeService(result, item, currRequest, orderResponse);

						// сервисы ресурса для сохранения
						ResourceService resourceService = createResourceService(created, user,
								resourceOrder.getResourceId(), item, ServiceStatus.NEW_ERROR,
								orderResponse.getError().getMessage());
						resourceOrder.addResourceService(resourceService);
						
						// обрабатываем данные по окончанию обработки сервиса
						orderResponseHelper.afterService(result, item, resourceService, currRequest, orderResponse);
					}
					result.getServices().addAll(currRequest.getServices());
				} else {
					for (ServiceItem item : orderResponse.getServices()) {
						
						// подготавливаем данные во время начала обработки сервиса
						orderResponseHelper.beforeService(result, item, currRequest, orderResponse);
						ResourceService resourceService = null;
						if (item.getError() == null) {
							
							// проверяем время на выкуп
							if (item.getExpire() == null) {
								item.setExpire(expire);
							}
							// проверяем возможность аннулирования заказа
							if (item.getCanceled() == null) {
								item.setCanceled(true);
							}
							resourceService = createResourceService(created, user, resourceOrder.getResourceId(), item, ServiceStatus.NEW, null);
						} else {
							resourceService = createResourceService(created, user, resourceOrder.getResourceId(), item, ServiceStatus.NEW_ERROR, item.getError().getMessage());
						}
						resourceOrder.addResourceService(resourceService);
						
						// обрабатываем данные по окончанию обработки сервиса
						orderResponseHelper.afterService(result, item, resourceService, currRequest, orderResponse);
						result.getServices().add(item);
					}
				}
				// обрабатываем данные после обработкой сервисов
				orderResponseHelper.afterServices(result, currRequest, orderResponse);
			}
		}
		// обрабатываем данные после обработкой сервисов
		orderResponseHelper.afterOrder(originalRequest, result, resuorcesRequests, resourcesResponses, order);
		result.fillMaps();
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
	
	public void addReturnConditions(ServiceItem item, Price price) {
		if (item.getPrice() != null
				&& item.getPrice().getTariff() != null
				&& (item.getPrice().getTariff().getReturnConditions() == null
					|| item.getPrice().getTariff().getReturnConditions().isEmpty())
				&& price != null
				&& price.getTariff() != null) {
			item.getPrice().getTariff().setReturnConditions(price.getTariff().getReturnConditions());
		}
	}
	
	private ResourceService createResourceService(Date created, User user, long resourceId, ServiceItem service,
			ServiceStatus statusType, String error) {
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
	
	public OrderResponse getResponse(Order order) {
		OrderResponse response = (OrderResponse) SerializationUtils.deserialize(SerializationUtils.serialize(order.getResponse()));
		return convertResponse(order, response);
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
		
		ReturnHelper returnHelper = ContextProvider.getBean(ReturnHelper.class);
		for (ServiceItem service : response.getServices()) {
			if (service.getError() == null) {
				returnHelper.calculateReturn(order, service);
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
	
	public ServiceItem getOrderService(Order order, ServiceItem service) {
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
		} else if ((service.getCanceled() == null || !service.getCanceled())
				&& (newData.getCanceled() == null || newData.getCanceled())) {
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
		response.fillMaps();
		return response;
	}
	
	public Order joinOrders(Order presentOrder, Order newOrder) {
		
		// добавляем новые заказы ресурса в существующий
		for (ResourceOrder resourceOrder : newOrder.getOrders()) {
			presentOrder.addResourceOrder(resourceOrder);
		}
		for (OrderClient client : newOrder.getClients()) {
			presentOrder.addOrderClient(client);
		}
		// обновляем словари
		presentOrder.getResponse().join(newOrder.getResponse());
		return presentOrder;
	}
	
	public Order removeServices(Order order, List<ServiceItem> removed) {
		return removeServices(order, removed, true);
	}
	
	public Order removeServices(Order order, List<ServiceItem> removed, boolean allMustPresent) {
		Date created = new Date();
		User user = dataController.getUser();
		for (ResourceOrder resourceOrder : order.getOrders()) {
			Set<String> resourceServiceIds = removed.stream().map(ServiceItem::getId).collect(Collectors.toSet());
			if (anyMatchServiceId(resourceOrder.getServices(), removed)) {
				if (!allMustPresent
						|| resourceOrder.getServices().stream().allMatch(rs -> resourceServiceIds.contains(String.valueOf(rs.getId())))) {
					for (ResourceService resourceService : resourceOrder.getServices()) {
						
						if (allMustPresent
								|| anyMatchServiceId(Collections.singleton(resourceService), removed)) {
							
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
					}
				} else {
					throw new RequestValidateException("Services with ids ["
							+ String.join(", ", resourceOrder.getServices().stream().map(rs -> String.valueOf(rs.getId())).collect(Collectors.toSet()))
							+ "] must be removed together");
				}
			}
		}
		//TODO remove customer from order clients
		order.getResponse().fillMaps();
		return order;
	}
	
	private boolean anyMatchServiceId(Set<ResourceService> resourceServices, List<ServiceItem> serviceItems) {
		return resourceServices.stream().anyMatch(rs -> {
			for (ServiceItem serviceItem : serviceItems) {
				if (isServiceOfResourceService(serviceItem, rs)) {
					return true;
				}
			}
			return false;
		});
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
		
		// максимально последний статус любой позиции заказа
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
	
	public OrderResponse removeExistingServices(OrderResponse response, OrderRequest request) {
		if (response.getServices() == null
				|| request.getServices() == null
				|| request.getServices().isEmpty()) {
			return response;
		}
		response.getServices().removeIf(s -> request.getServices().stream().noneMatch(rs -> rs.getId().equals(s.getId())));
		response.fillMaps();
		return response;
	}
	
	public void replaceResponseWithConverted(Order order) {
		order.setResponse(convertResponse(order, order.getResponse()));
	}

}
