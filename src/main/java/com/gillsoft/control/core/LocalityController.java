package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.ApiException;
import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.core.data.DataConverter;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.AgregatorLocalityService;
import com.gillsoft.control.service.model.LocalityType;
import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocalityController {
	
	private static Logger LOGGER = LogManager.getLogger(LocalityController.class);
	
	@Autowired
	private AgregatorLocalityService service;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private MappingService mappingService;
	
	private Map<Long, Set<Locality>> allNotMapped = new HashMap<>();
	
	private Map<Long, Set<Locality>> usedNotMapped = new HashMap<>();
	
	public Map<Long, Set<Locality>> getAllNotMapped() {
		return allNotMapped;
	}

	public Map<Long, Set<Locality>> getUsedNotMapped() {
		return usedNotMapped;
	}

	public List<Locality> getAll(LocalityRequest mainRequest) {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_ALL, MethodType.POST, mainRequest);
		List<LocalityResponse> responses = service.getAll(requests);
		return getMapping(requests, responses, mainRequest, allNotMapped);
	}
	
	public List<Locality> getUsed(LocalityRequest mainRequest) {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_USED, MethodType.POST, mainRequest);
		List<LocalityResponse> responses = service.getUsed(requests);
		return getMapping(requests, responses, mainRequest, usedNotMapped);
	}
	
	private List<Locality> getMapping(List<LocalityRequest> requests, List<LocalityResponse> responses,
			LocalityRequest mainRequest, Map<Long, Set<Locality>> notMapped) {
		if (responses == null) {
			return new ArrayList<>(0);
		}
		// сюда складываем уникальный маппинг по каждому запрашиваемому ресурсу
		Map<Long, Mapping> mappings = new HashMap<>();
		
		for (LocalityResponse localityResponse : responses) {
			if (!Utils.isError(LOGGER, localityResponse)
					&& localityResponse.getLocalities() != null
					&& !localityResponse.getLocalities().isEmpty()) {
				Stream<LocalityRequest> stream = requests.stream().filter(request -> request.getId().equals(localityResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					LocalityRequest request = stream.findFirst().get();
					
					// мапинг ресурса
					long resourceId = Long.parseLong(request.getParams().getResource().getId());
					mappings.putAll(localityResponse.getLocalities().parallelStream()
							.map(l -> {
								List<Mapping> mapped = mappingService.getMappings(MapType.GEO, resourceId, l.getId(), mainRequest.getLang());
								if (mapped == null) {
									if (!notMapped.containsKey(resourceId)) {
										notMapped.put(resourceId, new HashSet<>());
									}
									notMapped.get(resourceId).add(l);
								}
								return mapped;
							})
							.filter(map -> map != null).flatMap(maps -> maps.stream()).collect(
									Collectors.toMap(Mapping::getId, m -> m, (m1, m2) -> m1)));
				}
			}
		}
		// преобразовываем Mapping в Locality
		Map<String, Locality> localities = new HashMap<>();
		for (Mapping mapping : mappings.values()) {
			addLocality(mapping, mainRequest.getLang(), localities);
		}
		setLocalitiesType(localities);
		return new ArrayList<>(localities.values());
	}
	
	private void setLocalitiesType(Map<String, Locality> localities) {
		for (Locality locality : localities.values()) {
			if (locality.getParent() == null) {
				locality.setType(LocalityType.COUNTRY.name());
			} else {
				Locality parent = localities.get(locality.getParent().getId());
				if (parent != null
						&& LocalityType.LOCALITY.name().equals(parent.getType())) {
					locality.setType(LocalityType.STOPPING.name());
				}
			}
		}
	}
	
	public void addLocality(Mapping mapping, Lang lang, Map<String, Locality> localities) {
		if (!localities.containsKey(String.valueOf(mapping.getId()))) {
			localities.put(String.valueOf(mapping.getId()), createFullLocality(mapping, lang, null));
			if (mapping.getParent() != null) {
				addLocality(mapping.getParent(), lang, localities);
			}
		}
	}
	
	public Locality createFullLocality(Mapping mapping, Lang lang, Locality original) {
		Locality locality = DataConverter.createLocality(mapping, lang, original);
		locality.setParent(mapping.getParent() != null ? new Locality(String.valueOf(mapping.getParent().getId())): null);
		return locality;
	}
	
	public Locality getLocality(long mappingId) {
		Mapping mapping = mappingService.getMapping(mappingId);
		if (mapping != null) {
			return DataConverter.createLocality(mapping, null, null);
		} else {
			return null;
		}
	}
	
	public Map<String, Set<String>> getBinding(LocalityRequest mainRequest) {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_BINDING, MethodType.POST, mainRequest);
		List<LocalityResponse> responses = service.getBinding(requests);
		if (responses == null) {
			return new HashMap<>(0);
		}
		Map<String, Set<String>> fromToMapping = new HashMap<>();
		for (LocalityResponse localityResponse : responses) {
			if (!Utils.isError(LOGGER, localityResponse)
					&& localityResponse.getBinding() != null
					&& !localityResponse.getBinding().isEmpty()) {
				Stream<LocalityRequest> stream = requests.stream().filter(request -> request.getId().equals(localityResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					LocalityRequest request = stream.findFirst().get();
				
					// получаем все уникальные ид ресурса
					Set<String> resourceIds = new HashSet<>();
					for (Entry<String, List<String>> entry : localityResponse.getBinding().entrySet()) {
						resourceIds.add(entry.getKey());
						resourceIds.addAll(entry.getValue());
					}
					// формируем мапинг
					long resourceId = Long.parseLong(request.getParams().getResource().getId());
					Map<String, Set<Long>> mapping = resourceIds.stream().collect(
							Collectors.toMap(id -> id, id -> {
								Set<Long> ids = mappingService.getMappingIds(MapType.GEO, resourceId, id);
								return ids == null ? new HashSet<>() : ids;
							}, (ids1, ids2) -> ids1));
					
					// формируем пары маппинга from - to
					for (Entry<String, List<String>> entry : localityResponse.getBinding().entrySet()) {
						Set<Long> mappingIds = mapping.get(entry.getKey());
						if (mappingIds != null) {
							for (Long mappingId : mappingIds) {
								Set<String> tos = fromToMapping.get(mappingId);
								if (tos == null) {
									tos = new HashSet<>();
									fromToMapping.put(String.valueOf(mappingId), tos);
								}
								for (String id : entry.getValue()) {
									Set<Long> toMappingIds = mapping.get(id);
									if (toMappingIds != null) {
										tos.addAll(toMappingIds.stream().map(toId -> String.valueOf(toId)).collect(Collectors.toSet()));
									}
								}
							}
						}
					}
				}
			}
		}
		return fromToMapping;
	}
	
	private List<LocalityRequest> createRequest(String methodPath, MethodType methodType, LocalityRequest mainRequest) throws ApiException {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			List<LocalityRequest> request = new ArrayList<>();
			for (Resource resource : resources) {
				if (infoController.isMethodAvailable(resource, methodPath, methodType)) {
					LocalityRequest localityRequest = new LocalityRequest();
					localityRequest.setId(StringUtil.generateUUID());
					localityRequest.setParams(resource.createParams());
					request.add(localityRequest);
				}
			}
			if (request.isEmpty()) {
				throw new MethodUnavalaibleException("Method is unavailable");
			}
			return request;
		}
		throw new ResourceUnavailableException("User does not has available resources");
	}

}
