package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.service.PrintTicketService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.PrintOrderWrapper;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
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
	
	public void addSystemDocuments(Order order, Lang lang) {
		
		// макеты по ресурсам заказа и текущему пользователю
		Map<Long, List<TicketLayout>> layoutsMap = dataController.getTicketLayouts(order);
		
		// конвертируем заказ в ответ
		OrderResponse response = orderConverter.getResponse(order);
		
		// проставляем данные со словарей
		orderConverter.updateSegments(response);
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

}
