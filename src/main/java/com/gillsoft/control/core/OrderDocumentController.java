package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.PrintTicketService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.PrintOrderWrapper;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.ms.entity.TicketLayout;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OrderDocumentController {
	
	@Autowired
	private PrintTicketService printService;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private OrderResponseConverter orderConverter;
	
	@Autowired
	private LocalityController localityController;
	
	public void addSystemDocuments(Order order, Lang lang) {
		
		// макеты по ресурсам заказа и текущему пользователю
		Map<Long, List<TicketLayout>> layoutsMap = dataController.getTicketLayouts(order);
		
		// конвертируем заказ в ответ
		OrderResponse response = orderConverter.getResponse(order);
		
		// проставляем данные со словарей
		updateResponseData(response);
		List<ServiceItem> items = response.getServices();
		
		// формируем билет по каждой позиции отдельно
		for (ResourceOrder resourceOrder : order.getOrders()) {
			if (layoutsMap.containsKey(resourceOrder.getResourceId())) {
				List<TicketLayout> ticketLayouts = layoutsMap.get(resourceOrder.getResourceId());
				
				// сервисы, по которым уже создан билет
				Set<ResourceService> processedServices = new HashSet<>();
				for (ResourceService service : resourceOrder.getServices()) {
					if (!processedServices.contains(service)) {
						
						// берем статус, по которому формируется документ
						ServiceStatusEntity status = orderConverter.getLastNotErrorStatusEntity(service.getStatuses());
						if (status.getError() == null) {
							
							// берем макеты соответствующие последнему статусу
							if (ticketLayouts != null) {
								List<TicketLayout> layouts = ticketLayouts.stream().filter(l -> l.getServiceStatus() == status.getStatus()).collect(Collectors.toList());
								for (TicketLayout ticketLayout : layouts) {
									if (ticketLayout.getLayout() != null
											&& !ticketLayout.getLayout().isEmpty()) {
									
										// TODO может стоит брать юзера со статуса ???
										
										ServiceItem item = null;
										ServiceStatus orderStatus = null;
										List<ServiceItem> services = new ArrayList<>();
										switch (ticketLayout.getApplyingArea()) {
										
										// если печать для всего заказа, то выбираем все сервисы заказа ресурса со статусом текущей позиции
										case RESOURCE_ORDER:
											orderStatus = status.getStatus();
											for (ResourceService otherService : resourceOrder.getServices()) {
												ServiceStatusEntity otherStatus = orderConverter.getLastNotErrorStatusEntity(otherService.getStatuses());
												if (otherStatus.getError() == null
														&& status.getStatus() == otherStatus.getStatus()) {
													processedServices.add(otherService);
													services.add(items.stream().filter(s -> Long.valueOf(s.getId()) == otherService.getId()).findFirst().get());
												}
											}
											break;
										case SINGLE_SERVICE:
											processedServices.add(service);
											item = items.stream().filter(s -> Long.valueOf(s.getId()) == service.getId()).findFirst().get();
											services.add(item);
											break;
										default:
											break;
										}
										response.setServices(services);
										PrintOrderWrapper orderWrapper = new PrintOrderWrapper();
										orderWrapper.setLang(lang);
										orderWrapper.setOrder(response);
										orderWrapper.setTicketLayout(ticketLayout.getLayout());
										List<Document> documents = printService.create(orderWrapper);
										orderConverter.addDocuments(order, orderStatus, documents, item);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Заполняем сегменты данными из словарей.
	 * 
	 * @param response
	 *            Заказ.
	 */
	public void updateResponseData(OrderResponse response) {
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
			if (service.getAdditionalService() != null) {
				service.setAdditionalService(response.getAdditionalServices().get(service.getAdditionalService().getId()));
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

}
