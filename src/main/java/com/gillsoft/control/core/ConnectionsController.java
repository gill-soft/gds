package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.gillsoft.ms.entity.Resource;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ConnectionsController {

	@Autowired
	private SegmentsConnectionRestService restService;
	
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
	
	public ConnectionsResponse createConnections(TripSearchRequest request, List<Resource> resources) {
		return createConnections(createParams(request,
				resources.stream().map(Resource::getId).collect(Collectors.toSet())));
	}
	
	public void connectSegments(TripSearchResponse tripSearchResponse, SearchRequestContainer requestContainer) {
		if (tripSearchResponse.getSegments() == null) {
			return;
		}
		List<Trip> result = new ArrayList<>();
		for (List<Long> route : requestContainer.getConnections().getRoutes()) {
			List<List<String>> segments = new ArrayList<>(); // список возможных частей маршрута
			
			// идем по маршруту и находим рейсы-части
			long currPoint = route.get(0);
			for (int i = 1; i < route.size(); i++) {
				long nextPoint = route.get(i);
				List<String> currPart = getTripIds(currPoint, nextPoint, tripSearchResponse);
				if (currPart.isEmpty()) {
					break;
				}
				segments.add(currPart);
				currPoint = nextPoint;
			}
			// соединяем сегменты с учетом времени пересадки
			if (segments.size() == route.size() - 1) {
				joinSegments(tripSearchResponse, requestContainer, result, segments);
			}
		}
		if (!result.isEmpty()) {
			
			// результат соединений
			TripContainer container = new TripContainer();
			container.setRequest(requestContainer.getOriginRequest());
			container.getRequest().setSearchCompleted(true);
			container.setTrips(result);
			tripSearchResponse.getTripContainers().add(container);
		}
	}
	
	private void joinSegments(TripSearchResponse tripSearchResponse, SearchRequestContainer requestContainer,
			List<Trip> result, List<List<String>> segments) {
		
		// соединяем сегменты с учетом времени пересадки
		List<String> first = segments.get(0);
		List<Trip> trips = new ArrayList<>(first.size()); // соединенные в рейсы части маршрута
		for (String id : first) {
			Trip trip = new Trip();
			trip.setSegments(Collections.singletonList(id));
			trips.add(trip);
		}
		for (int i = 1; i < segments.size(); i++) {
			List<Trip> nextParts = new ArrayList<>();
			List<String> next = segments.get(i);
			for (String id : next) {
				
				// берем последний сегмент с рейса и пробуем присоединить текущую часть
				for (Trip trip : trips) {
					if (isConnected(requestContainer.getConnections().getConnections(), tripSearchResponse,
							trip.getSegments().get(trip.getSegments().size() - 1), id)) {
						Trip newTrip = new Trip();
						newTrip.setSegments(new ArrayList<>(trip.getSegments()));
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
		// создаем md5 ид
		result.addAll(trips);
	}
	
	private boolean isConnected(List<SegmentConnection> connections, TripSearchResponse tripSearchResponse,
			String fromSegmentId, String toSegmentId) {
		Segment fromSegment = tripSearchResponse.getSegments().get(fromSegmentId);
		Segment toSegment = tripSearchResponse.getSegments().get(toSegmentId);
		if (toSegment.getDepartureDate() == null
				|| fromSegment.getArrivalDate() == null) {
			return false;
		}
		for (SegmentConnection connection : connections) {
			Locality departure = tripSearchResponse.getLocalities().get(fromSegment.getArrival().getId());
			Locality arrival = tripSearchResponse.getLocalities().get(toSegment.getDeparture().getId());
			
			// проверяем возможность пересадки
			if (isEqualsPoints(String.valueOf(connection.getFrom()), departure)
					&& isEqualsPoints(String.valueOf(connection.getTo()), arrival)) {
				
				// проверяем время пересадки
				int betweenSegments = (int) ((toSegment.getDepartureDate().getTime() - fromSegment.getArrivalDate().getTime()) / 60000);
				if (betweenSegments >= connection.getMinConnectionTime()
						&& (connection.getMaxConnectionTime() == 0
						|| betweenSegments <= connection.getMaxConnectionTime())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private List<String> getTripIds(long from, long to, TripSearchResponse tripSearchResponse) {
		List<String> segmentIds = new ArrayList<>();
		for (TripContainer container : tripSearchResponse.getTripContainers()) {
			if (container.getRequest().isSearchCompleted()) {
				for (Entry<String, Segment> entry : tripSearchResponse.getSegments().entrySet()) {
					Locality departure = tripSearchResponse.getLocalities().get(entry.getValue().getDeparture().getId());
					Locality arrival = tripSearchResponse.getLocalities().get(entry.getValue().getArrival().getId());
					if (isEqualsPoints(String.valueOf(from), departure)
							&& isEqualsPoints(String.valueOf(to), arrival)) {
						segmentIds.add(entry.getKey());
					}
				}
			}
		}
		return segmentIds;
	}
	
	private boolean isEqualsPoints(String pointId, Locality locality) {
		if (Objects.equals(locality.getId(), pointId)) {
			return true;
		}
		while (locality.getParent() != null) {
			locality = locality.getParent();
			if (Objects.equals(locality.getId(), pointId)) {
				return true;
			}
		}
		return false;
	}
	
}
