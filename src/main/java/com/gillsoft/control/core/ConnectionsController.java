package com.gillsoft.control.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.service.impl.SegmentsConnectionRestService;
import com.gillsoft.control.service.model.ConnectionParams;
import com.gillsoft.control.service.model.ConnectionsResponse;
import com.gillsoft.control.service.model.SearchRequestContainer;
import com.gillsoft.control.service.model.SegmentConnection;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.ms.entity.EntityType;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ResourceConnection;
import com.gillsoft.ms.entity.ServiceFilter;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ConnectionsController {
	
	@Autowired
	private MsDataController dataController;

	@Autowired
	private SegmentsConnectionRestService restService;
	
	@Autowired
	@Qualifier("FilterController")
	private FilterController filter;
	
	public ConnectionsResponse createConnections(TripSearchRequest request, List<Resource> resources) {
		return createConnections(createParams(request,
				resources.stream().map(Resource::getId).collect(Collectors.toSet())));
	}
	
	private ConnectionsResponse createConnections(ConnectionParams params) {
		if (params == null) {
			return null;
		}
		return restService.getConnections(params);
	}
	
	private ConnectionParams createParams(TripSearchRequest request, Set<Long> resources) {
		try {
			ConnectionParams params = new ConnectionParams();
			params.setFrom(Integer.parseInt(request.getLocalityPairs().get(0)[0]));
			params.setTo(Integer.parseInt(request.getLocalityPairs().get(0)[1]));
			params.setMaxConnections(request.getMaxConnections());
			params.setMinConnectionTime(request.getMinConnectionTime());
			params.setMinConnectionTime(request.getMaxConnectionTime());
			params.setResources(resources);
			return params;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public void connectSegments(TripSearchResponse tripSearchResponse, SearchRequestContainer requestContainer) {
		if (tripSearchResponse.getSegments() == null) {
			return;
		}
		// получаем разрешенные к стыковке ресурсы
		Map<Long, List<ResourceConnection>> resourceConnectionsMap = null;
		List<ResourceConnection> resourceConnections = dataController.getResourceConnections();
		if (resourceConnections != null) {
			resourceConnectionsMap = resourceConnections.stream()
					.collect(Collectors.groupingBy(rc -> rc.getResource().getId(), Collectors.toList()));
		}
		List<Trip> result = new LinkedList<>();
		for (List<Long> route : requestContainer.getConnections().getRoutes()) {
			List<Set<String>> segments = new LinkedList<>(); // список возможных частей маршрута
			
			// идем по маршруту и находим рейсы-части
			long currPoint = route.get(0);
			for (int i = 1; i < route.size(); i++) {
				long nextPoint = route.get(i);
				Set<String> currPart = getTripIds(currPoint, nextPoint, tripSearchResponse);
				if (currPart.isEmpty()) {
					break;
				}
				segments.add(currPart);
				currPoint = nextPoint;
			}
			// соединяем сегменты с учетом времени пересадки
			if (segments.size() == route.size() - 1) {
				joinSegments(resourceConnectionsMap, tripSearchResponse, requestContainer, result, segments);
			}
		}
		if (!result.isEmpty()) {
			
			// результат соединений
			TripContainer container = new TripContainer();
			container.setRequest(requestContainer.getOriginRequest());
			container.getRequest().setPermittedToResult(true);
			container.setTrips(result);
			tripSearchResponse.getTripContainers().add(container);
		}
	}
	
	private void joinSegments(Map<Long, List<ResourceConnection>> resourceConnectionsMap,
			TripSearchResponse tripSearchResponse, SearchRequestContainer requestContainer, List<Trip> result,
			List<Set<String>> segments) {
		
		// соединяем сегменты с учетом времени пересадки
		Set<String> first = segments.get(0);
		List<Trip> trips = new LinkedList<>(); // соединенные в рейсы части маршрута
		for (String id : first) {
			Trip trip = new Trip();
			trip.setSegments(Collections.singletonList(id));
			trips.add(trip);
		}
		for (int i = 1; i < segments.size(); i++) {
			List<Trip> nextParts = new LinkedList<>();
			Set<String> next = segments.get(i);
			for (String id : next) {
				
				// берем последний сегмент с рейса и пробуем присоединить текущую часть
				for (Trip trip : trips) {
					if (isConnected(resourceConnectionsMap, requestContainer.getConnections().getConnections(), tripSearchResponse,
							trip.getSegments().get(trip.getSegments().size() - 1), id)) {
						Trip newTrip = new Trip();
						newTrip.setSegments(new LinkedList<>(trip.getSegments()));
						newTrip.getSegments().add(id);
						nextParts.add(newTrip);
					}
				}
			}
			trips = nextParts;
			if (trips.isEmpty()) {
				break;
			}
		}
		// создаем md5 ид предыдущих частей - нужно для определения последовальности сегментов в заказе
		updateIds(tripSearchResponse, trips);
		
		result.addAll(trips);
	}
	
	private void updateIds(TripSearchResponse tripSearchResponse, List<Trip> trips) {
		Map<String, TripIdModel> newIds = new HashMap<>();
		for (Trip trip : trips) {
			for (int i = 0; i < trip.getSegments().size() - 1; i++) {
				TripIdModel id = newIds.get(trip.getSegments().get(i));
				if (id == null) {
					id = new TripIdModel().create(trip.getSegments().get(i));
					newIds.put(trip.getSegments().get(i), id);
				}
				TripIdModel next = new TripIdModel().create(trip.getSegments().get(i + 1));
				next.setNext(null);
				if (id.getNext() == null) {
					id.setNext(new HashSet<>());
				}
				id.getNext().add(StringUtil.md5(next.asString()));
			}
		}
		Map<String, String> newStrIds = new HashMap<>(newIds.size());
		newIds.forEach((k, v) -> newStrIds.put(k, v.asString()));
		for (Trip trip : trips) {
			for (int i = 0; i < trip.getSegments().size(); i++) {
				if (newStrIds.containsKey(trip.getSegments().get(i))) {
					String newId = newStrIds.get(trip.getSegments().get(i));
					tripSearchResponse.getSegments().put(newId, tripSearchResponse.getSegments().get(trip.getSegments().get(i)));
					trip.getSegments().set(i, newId);
				}
			}
		}
	}
	
	private boolean isConnected(Map<Long, List<ResourceConnection>> resourceConnectionsMap,
			List<SegmentConnection> connections, TripSearchResponse tripSearchResponse, String fromSegmentId,
			String toSegmentId) {
		Segment fromSegment = tripSearchResponse.getSegments().get(fromSegmentId);
		Segment toSegment = tripSearchResponse.getSegments().get(toSegmentId);
		
		// проверяем даты стыковки
		if (toSegment.getDepartureDate() == null
				|| fromSegment.getArrivalDate() == null) {
			return false;
		}
		// проверяем разрешенные стыковки ресурсов
		if (resourceConnectionsMap != null
				&& (!isResourceConnectionAvailable(resourceConnectionsMap, fromSegment, toSegment)
						|| !isResourceConnectionAvailable(resourceConnectionsMap, toSegment, fromSegment))) {
			return false;
		}
		int betweenSegments = (int) ((toSegment.getDepartureDate().getTime() - fromSegment.getArrivalDate().getTime()) / 60000);
		if (betweenSegments > 0) {
			String departureId = fromSegment.getArrival().getId();
			String arrivalId = toSegment.getDeparture().getId();
			for (SegmentConnection connection : connections) {
				
				// проверяем возможность пересадки
				if (isEqualsPoints(String.valueOf(connection.getFrom()), departureId, tripSearchResponse.getLocalities())
						&& isEqualsPoints(String.valueOf(connection.getTo()), arrivalId, tripSearchResponse.getLocalities())
						&& isCarriersEnabled(fromSegment, toSegment, connection)
						&& isResourcesEnabled(fromSegment, toSegment, connection)) {
					
					// проверяем время пересадки
					if (betweenSegments >= connection.getMinConnectionTime()
							&& (connection.getMaxConnectionTime() == 0
							|| betweenSegments <= connection.getMaxConnectionTime())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean isResourceConnectionAvailable(Map<Long, List<ResourceConnection>> resourceConnectionsMap, Segment fromSegment, Segment toSegment) {
		List<ResourceConnection> resourceConnectionFroms = resourceConnectionsMap.get(Long.parseLong(fromSegment.getResource().getId()));
		if (resourceConnectionFroms == null) {
			return true;
		}
		// если хоть одно условие разрешает стыковку, то разрешаем
		boolean enabled = false;
		for (ResourceConnection resourceConnectionFrom : resourceConnectionFroms) {
			
			// фильтры главных рейсов
			List<ServiceFilter> filters = resourceConnectionFrom.getChilds().stream()
					.filter(child -> child.getType() == EntityType.FILTER).map(child -> (ServiceFilter) child).collect(Collectors.toList());
			if (!filters.isEmpty()
					&& filter.isRemove(fromSegment, filters)) {
				continue;
			}
			// условия фильтрации зависимых рейсов
			Set<Long> resources = resourceConnectionFrom.getChilds().stream()
					.filter(child -> child.getType() == EntityType.RESOURCE).map(child -> child.getId()).collect(Collectors.toSet());
			if (resourceConnectionFrom.isEnable()) {
				enabled = enabled || resources.contains(Long.parseLong(toSegment.getResource().getId()));
			} else {
				enabled = enabled || !resources.contains(Long.parseLong(toSegment.getResource().getId()));
			}
			if (enabled) {
				return true;
			}
		}
		return enabled;
	}
	
	private Set<String> getTripIds(long from, long to, TripSearchResponse tripSearchResponse) {
		Set<String> segmentIds = new HashSet<>();
		for (TripContainer container : tripSearchResponse.getTripContainers()) {
			if (container.getRequest().isPermittedToResult()) {
				for (Entry<String, Segment> entry : tripSearchResponse.getSegments().entrySet()) {
					if (!segmentIds.contains(entry.getKey())
							&& entry.getValue().getDeparture() != null
							&& entry.getValue().getArrival() != null) {
						String departureId = entry.getValue().getDeparture().getId();
						String arrivalId = entry.getValue().getArrival().getId();
						if (isEqualsPoints(String.valueOf(from), departureId, tripSearchResponse.getLocalities())
								&& isEqualsPoints(String.valueOf(to), arrivalId, tripSearchResponse.getLocalities())) {
							segmentIds.add(entry.getKey());
						}
					}
				}
			}
		}
		return segmentIds;
	}
	
	private boolean isEqualsPoints(String pointId, String segmentPointId, Map<String, Locality> localities) {
		Locality locality = localities.get(segmentPointId);
		if (Objects.equals(locality.getId(), pointId)) {
			return true;
		}
		while (locality.getParent() != null) {
			locality = localities.get(locality.getParent().getId());
			if (Objects.equals(locality.getId(), pointId)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isCarriersEnabled(Segment fromSegment, Segment toSegment, SegmentConnection connection) {
		String fromCarrier = fromSegment.getCarrier() != null ? fromSegment.getCarrier().getId() : null;
		String toCarrier = toSegment.getCarrier() != null ? toSegment.getCarrier().getId() : null;
		return (connection.getFromCarriers() == null
				|| connection.getFromCarriers().isEmpty()
				|| connection.getFromCarriers().contains(fromCarrier))
				&& (connection.getToCarriers() == null
						|| connection.getToCarriers().isEmpty()
						|| connection.getToCarriers().contains(toCarrier));
	}
	
	private boolean isResourcesEnabled(Segment fromSegment, Segment toSegment, SegmentConnection connection) {
		String fromResource = fromSegment.getResource() != null ? fromSegment.getResource().getId() : null;
		String toResource = toSegment.getResource() != null ? toSegment.getResource().getId() : null;
		return (connection.getFromResources() == null
				|| connection.getFromResources().isEmpty()
				|| connection.getFromResources().contains(fromResource))
				&& (connection.getToResources() == null
						|| connection.getToResources().isEmpty()
						|| connection.getToResources().contains(toResource));
	}
	
}
