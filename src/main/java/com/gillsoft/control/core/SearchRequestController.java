package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.model.ConnectionsResponse;
import com.gillsoft.control.service.model.Pair;
import com.gillsoft.control.service.model.SearchRequestContainer;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.ms.entity.EntityType;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ResourceConnection;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class SearchRequestController {
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private MappingService mappingService;
	
	@Autowired
	private ConnectionsController connectionsController;
	
	public SearchRequestContainer createSearchRequest(TripSearchRequest searchRequest) {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			SearchRequestContainer requestContainer = new SearchRequestContainer();
			searchRequest.setToResult(true);
			requestContainer.setOriginRequest(searchRequest);
			
			// проверяем запрос на поиск стыковочных рейсов и делаем пары с учетом стыковок
			// если в запросе больше одной пары пунктов и несколько дат, то поиск стыковочных рейсов не выполняем
			if (searchRequest.isUseTranfers()
					&& searchRequest.getLocalityPairs().size() == 1
					&& searchRequest.getDates().size() == 1) {
				addConnectionRequests(searchRequest, requestContainer, resources);
			}
			for (Resource resource : resources) {
				if (infoController.isMethodAvailable(resource, Method.SEARCH, MethodType.POST)) {
					
					// проверяем маппинг и формируем запрос на каждый ресурс
					for (String[] pair : searchRequest.getLocalityPairs()) {
						addSearchRequest(searchRequest, resource, pair, requestContainer, 0, null, true);
					}
				}
			}
			if (requestContainer.isEmpty()) {
				throw new MethodUnavalaibleException("Method is unavailable");
			}
			return requestContainer;
		}
		throw new ResourceUnavailableException("User does not has available resources");
	}
	
	private void addSearchRequest(TripSearchRequest searchRequest, Resource resource, String[] pair,
			SearchRequestContainer requestContainer, int addedDays, Integer maxConnections, boolean toResult) {
		Set<String> fromIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pair[0]));
		if (fromIds != null) {
			Set<String> toIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pair[1]));
			if (toIds != null) {
				TripSearchRequest resourceSearchRequest = new TripSearchRequest();
				
				// оригинальный ид, который будеи в ответе, плюс уникальный ид запроса
				resourceSearchRequest.setId(searchRequest.getId() + ";" + StringUtil.generateUUID());
				resourceSearchRequest.setToResult(toResult);
				resourceSearchRequest.setParams(resource.createParams());
				resourceSearchRequest.setDates(addDays(searchRequest.getDates(), addedDays));
				resourceSearchRequest.setMaxConnections(maxConnections);
				if (!searchRequest.isUseTranfers()) {
					resourceSearchRequest.setBackDates(searchRequest.getBackDates());
				}
				resourceSearchRequest.setCurrency(searchRequest.getCurrency());
				resourceSearchRequest.setLocalityPairs(new ArrayList<>());
				resourceSearchRequest.setLang(searchRequest.getLang());
				for (String fromId : fromIds) {
					for (String toId : toIds) {
						resourceSearchRequest.getLocalityPairs().add(new String[] { fromId, toId });
					}
				}
				requestContainer.add(String.join(";", pair), resourceSearchRequest);
			}
		}
	}
	
	private List<Date> addDays(List<Date> dates, int addedDays) {
		if (addedDays == 0) {
			return dates;
		}
		List<Date> newDates = new ArrayList<>(dates.size());
		for (Date date : dates) {
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			c.add(Calendar.DATE, addedDays);
			newDates.add(c.getTime());
		}
		return newDates;
	}
	
	private void addConnectionRequests(TripSearchRequest searchRequest, SearchRequestContainer requestContainer,
			List<Resource> resources) {
		ConnectionsResponse connections = connectionsController.createConnections(searchRequest, resources);
		if (connections != null) {
			requestContainer.setConnections(connections);
			for (Pair pair : filterPairs(connections)) {
				Optional<Resource> res = resources.stream().filter(r -> r.getId() == pair.getResourceId()).findFirst();
				if (res.isPresent()) {
					Resource resource = res.get();
					if (infoController.isMethodAvailable(resource, Method.SEARCH, MethodType.POST)) {
						addSearchRequest(searchRequest, resource,
								new String[]{ String.valueOf(pair.getFrom()), String.valueOf(pair.getTo()) },
								requestContainer, pair.getAddedDays(), searchRequest.getMaxConnections(), false);
					}
				}
			}
		}
	}
	
	private Set<Pair> filterPairs(ConnectionsResponse connections) {
		
		// получаем разрешенные к стыковке ресурсы
		List<ResourceConnection> resourceConnections = dataController.getResourceConnections();
		if (resourceConnections == null) {
			return connections.getPairs();
		}
		Map<Long, ResourceConnection> resourceConnectionsMap = dataController.getResourceConnections().stream()
				.collect(Collectors.toMap(rc -> rc.getResource().getId(), rc -> rc));
		
		Set<Pair> newPairs = new HashSet<>(); // список разрешенных пар поиска
		for (List<Long> route : connections.getRoutes()) {
			out: {
				Set<Pair> routePairs = new HashSet<>(); // список частей маршрута
				
				// идем по маршруту и находим рейсы-части
				Set<Pair> currPairs = null; // список частей маршрута
				long currPoint = route.get(0);
				for (int i = 1; i < route.size(); i++) {
					long nextPoint = route.get(i);
					Set<Pair> nextPairs = getPairs(connections.getPairs(), currPoint, nextPoint);
					if (nextPairs.isEmpty()) {
						break;
					} else if (currPairs != null) {
						
						// если стыковать нельзя, то не отбрасываем весь маршрут
						createAvailablePairs(resourceConnectionsMap, currPairs, nextPairs);
						if (currPairs.isEmpty()
								|| nextPairs.isEmpty()) {
							break out;
						}
						routePairs.addAll(currPairs);
						routePairs.addAll(nextPairs);
					}
					currPairs = nextPairs;
					currPoint = nextPoint;
				}
				newPairs.addAll(routePairs);
			}
		}
		return newPairs;
	}
	
	/*
	 * Сравнивает пары для стыковки.
	 */
	private void createAvailablePairs(Map<Long, ResourceConnection> resourceConnections, Set<Pair> currPairs, Set<Pair> nextPairs) {
		Set<Pair> newCurrPairs = new HashSet<>();
		Set<Pair> newNextPairs = new HashSet<>();
		for (Pair pairFrom : currPairs) {
			for (Pair pairTo : nextPairs) {
				if (isPairsAvailable(resourceConnections, pairFrom, pairTo)
						&& isPairsAvailable(resourceConnections, pairTo, pairFrom)) {
					newCurrPairs.add(pairFrom);
					newNextPairs.add(pairTo);
				}
			}
		}
		currPairs.clear();
		currPairs.addAll(newCurrPairs);
		nextPairs.clear();
		nextPairs.addAll(newNextPairs);
	}
	
	/*
	 * Возвращает список пар для указанных пунктов.
	 */
	private Set<Pair> getPairs(Set<Pair> pairs, long fromId, long toId) {
		Set<Pair> pairsPart = new HashSet<>();
		for (Pair pair : pairs) {
			Mapping from = mappingService.getMapping(pair.getFrom());
			Mapping to = mappingService.getMapping(pair.getTo());
			if (from != null && to != null
					&& isEqualsPoints(fromId, from)
					&& isEqualsPoints(toId, to)) {
				pairsPart.add(pair);
			}
		}
		return pairsPart;
	}
	
	private boolean isEqualsPoints(long pointId, Mapping mapping) {
		if (mapping.getId() == pointId) {
			return true;
		}
		while (mapping.getParent() != null) {
			mapping = mapping.getParent();
			if (mapping.getId() == pointId) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Проверяем можно ли стыковать пару.
	 */
	private boolean isPairsAvailable(Map<Long, ResourceConnection> resourceConnections, Pair pairFrom, Pair pairTo) {
		ResourceConnection resourceConnectionFrom = resourceConnections.get(pairFrom.getResourceId());
		if (resourceConnectionFrom == null) {
			return true;
		}
		// фильтры главных рейсов
		if (resourceConnectionFrom.getChilds().stream()
				.filter(child -> child.getType() == EntityType.FILTER).findFirst().isPresent()) {
			return true;
		}
		// условия фильтрации зависимых рейсов
		Set<Long> resources = resourceConnectionFrom.getChilds().stream()
				.filter(child -> child.getType() == EntityType.RESOURCE).map(child -> child.getId()).collect(Collectors.toSet());
		return resources.contains(pairTo.getResourceId());
	}

}
