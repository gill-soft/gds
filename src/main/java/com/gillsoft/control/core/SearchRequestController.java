package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.service.model.ConnectionsResponse;
import com.gillsoft.control.service.model.Pair;
import com.gillsoft.control.service.model.SearchRequestContainer;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.ms.entity.Resource;
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
						addSearchRequest(searchRequest, resource, pair, requestContainer, 0);
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
	
	public void addSearchRequest(TripSearchRequest searchRequest, Resource resource, String[] pair,
			SearchRequestContainer requestContainer, int addedDays) {
		Set<String> fromIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pair[0]));
		if (fromIds != null) {
			Set<String> toIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pair[1]));
			if (toIds != null) {
				TripSearchRequest resourceSearchRequest = new TripSearchRequest();
				
				// оригинальный ид, который будеи в ответе, плюс уникальный ид запроса
				resourceSearchRequest.setId(searchRequest.getId() + ";" + StringUtil.generateUUID());
				resourceSearchRequest.setParams(resource.createParams());
				resourceSearchRequest.setDates(addDays(searchRequest.getDates(), addedDays));
				resourceSearchRequest.setMaxConnections(searchRequest.getMaxConnections());
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
				requestContainer.add(resourceSearchRequest);
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
			for (Pair pair : connections.getPairs()) {
				Stream<Resource> res = resources.stream().filter(r -> r.getId() == pair.getResourceId());
				if (res != null) {
					Resource resource = res.findFirst().get();
					if (infoController.isMethodAvailable(resource, Method.SEARCH, MethodType.POST)) {
						addSearchRequest(searchRequest, resource,
								new String[]{ String.valueOf(pair.getFrom()), String.valueOf(pair.getTo()) }, requestContainer, pair.getAddedDays());
					}
				}
			}
		}
	}

}
