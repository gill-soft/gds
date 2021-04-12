package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.core.OrderResponseHelper.ServiceOrderResponseHandler;
import com.gillsoft.control.core.data.DataConverter;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.core.mapping.TripIdModel;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.Trip;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SegmentOrderResponseHandler implements ServiceOrderResponseHandler {
	
	@Autowired
	private OrderResponseConverter orderResponseConverter;
	
	@Autowired
	private TripSearchController searchController;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private DiscountController discountController;
	
	private Map<String, String> segmentIds;
	private Map<Long, com.gillsoft.ms.entity.Trip> mappedTrips;

	@Override
	public void beforeOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resuorcesRequests,
			OrderResponse resourcesResponses) {
		segmentIds = getSegmentIds(originalRequest);
		mappedTrips = new HashMap<>();
	}
	
	private Map<String, String> getSegmentIds(OrderRequest request) {
		return request.getServices().stream().filter(s -> s.getSegment() != null)
				.collect(Collectors.toMap(s -> {
					TripIdModel id = new TripIdModel().create(s.getSegment().getId());
					id.setNext(null);
					return id.asString();
				}, s -> s.getSegment().getId(), (s1, s2) -> s1));
	}

	@Override
	public void beforeServices(OrderResponse result, OrderRequest resuorceRequest, OrderResponse resourceResponse) {
		resourceResponse = (OrderResponse) SerializationUtils.deserialize(SerializationUtils.serialize(resourceResponse));
		if (resourceResponse.getError() != null) {
			
			// маппим рейсы заказа из запроса так как в ответе ошибка и их нет
			resourceResponse.setSegments(resuorceRequest.getServices().stream().map(
					s -> s.getSegment() != null ? s.getSegment().getId() : null).filter(v -> v != null).collect(
							Collectors.toMap(v -> v, v -> new Segment(), (v1, v2) -> v1)));
		}
		searchController.mapScheduleSegment(new ArrayList<>(segmentIds.keySet()), resuorceRequest, resourceResponse, result);
	}
	
	@Override
	public void beforeService(OrderResponse result, ServiceItem serviceItem, OrderRequest resuorceRequest,
			OrderResponse resourceResponse) {
		if (serviceItem.getSegment() != null
				&& result.getSegments() != null) {
			setSegment(result.getSegments(), serviceItem);
			Segment segment = result.getSegments().get(serviceItem.getSegment().getId());
			
			// пересчитываем стоимость
			if (serviceItem.getPrice() != null) {
				serviceItem.setPrice(dataController.recalculate(
						segment != null ? segment : null, serviceItem.getPrice(), resuorceRequest.getCurrency()));
			} else if (segment != null) {
				serviceItem.setPrice(segment.getPrice());
			}
			// добавляем условия возврата
			if (segment != null) {
				orderResponseConverter.addReturnConditions(serviceItem, segment.getPrice());
			}
		}
	}

	@Override
	public void afterService(OrderResponse result, ServiceItem serviceItem, ResourceService resourceService,
			OrderRequest resuorceRequest, OrderResponse resourceResponse) {
		addMappedTrips(serviceItem, resourceService);
	}
	
	private void setSegment(Map<String, Segment> segments, ServiceItem item) {
		for (String id : segments.keySet()) {
			TripIdModel model = new TripIdModel().create(id);
			if (Objects.equals(item.getSegment().getId(), model.getId())) {
				Segment segment = segments.get(id);
				segment.setId(id);
				item.setSegment(segment);
				break;
			}
		}
	}
	
	private void addMappedTrips(ServiceItem serviceItem, ResourceService resourceService) {
		if (serviceItem.getSegment() != null) {
			Segment segment = serviceItem.getSegment();
			resourceService.setDeparture(segment.getDepartureDate());
			Map<String, Object> additionals = segment.getAdditionals();
			if (additionals != null
					&& additionals.containsKey(MappedService.MAPPED_SERVICES_KEY)) {
				
				@SuppressWarnings("unchecked")
				List<MappedService> mappedServices = new ArrayList<>((Set<MappedService>) additionals.get(MappedService.MAPPED_SERVICES_KEY));
				Collections.sort(mappedServices, Comparator.comparing(MappedService::getOrder));
				long departureId = mappedServices.get(0).getFromId();
				long arrivalId = mappedServices.get(mappedServices.size() - 1).getToId();
				for (MappedService mappedService : mappedServices) {
					long tripId = mappedService.getTripId();
					com.gillsoft.ms.entity.Trip trip = mappedTrips.get(tripId);
					if (trip == null) {
						trip = dataController.getTripWithoutCache(tripId);
						mappedTrips.put(tripId, trip);
					}
					if (trip != null) {
						DataConverter.setTariff(mappedService, trip, serviceItem.getPrice(), departureId, arrivalId);
					}
				}
				mappedServices.forEach(ms -> resourceService.addMappedService(
						(MappedService) SerializationUtils.deserialize(SerializationUtils.serialize(ms))));
			}
		}
	}
	
	@Override
	public void afterServices(OrderResponse result, OrderRequest resuorceRequest, OrderResponse resourceResponse) {
		
	}

	@Override
	public void afterOrder(OrderRequest originalRequest, OrderResponse result, OrderRequest resuorcesRequests,
			OrderResponse resourcesResponses, Order order) {
		updateResultSegmentIds(result);
		applyConnectionDiscount(result, order);
		removePrices(result);
	}
	
	private void updateResultSegmentIds(OrderResponse result) {
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
	
	private void removePrices(OrderResponse result) {
		if (result.getSegments() != null) {
			result.getSegments().values().forEach(s -> s.setPrice(null));
		}
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
									if (orderResponseConverter.isServiceOfResourceService(service, resourceService)) {
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
									if (orderResponseConverter.isServiceOfResourceService(service, resourceService)) {
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

}
