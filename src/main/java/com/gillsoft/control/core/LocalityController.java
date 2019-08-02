package com.gillsoft.control.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
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
import com.gillsoft.control.service.AgregatorLocalityService;
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
	
	public List<Locality> getAll(LocalityRequest mainRequest) {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_ALL, MethodType.POST, mainRequest);
		List<LocalityResponse> responses = service.getAll(requests);
		return getMapping(requests, responses, mainRequest);
	}
	
	public List<Locality> getUsed(LocalityRequest mainRequest) {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_USED, MethodType.POST, mainRequest);
		List<LocalityResponse> responses = service.getUsed(requests);
		return getMapping(requests, responses, mainRequest);
	}
	
	private List<Locality> getMapping(List<LocalityRequest> requests, List<LocalityResponse> responses, LocalityRequest mainRequest) {
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
					mappings.putAll(localityResponse.getLocalities().stream()
							.map(l -> mappingService.getMappings(MapType.GEO, resourceId, l.getId(), mainRequest.getLang()))
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
		return new ArrayList<>(localities.values());
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
		Locality locality = createLocality(mapping, lang, original);
		locality.setParent(mapping.getParent() != null ? new Locality(String.valueOf(mapping.getParent().getId())): null);
		return locality;
	}
	
	public Locality createLocality(Mapping mapping, Lang lang, Locality original) {
		Locality locality = new Locality();
		locality.setId(String.valueOf(mapping.getId()));
		if (mapping.getAttributes() != null
				|| mapping.getLangAttributes() != null) {
			if (lang == null
					&& mapping.getLangAttributes() != null) {
				for (Entry<Lang, ConcurrentMap<String, String>> entry : mapping.getLangAttributes().entrySet()) {
					locality.setName(entry.getKey(), entry.getValue().containsKey("NAME") ?
							entry.getValue().get("NAME") : (original != null ? original.getName(entry.getKey()) : null));
					locality.setAddress(entry.getKey(), entry.getValue().containsKey("ADDRESS") ?
							entry.getValue().get("ADDRESS") : (original != null ? original.getAddress(entry.getKey()) : null));
				}
			} else if (mapping.getAttributes() != null) {
				if (mapping.getAttributes().containsKey("NAME")) {
					locality.setName(lang, mapping.getAttributes().get("NAME"));
				} else if (original != null) {
					locality.setName(original.getName());
				}
				if (mapping.getAttributes().containsKey("ADDRESS")) {
					locality.setAddress(lang, mapping.getAttributes().get("ADDRESS"));
				} else if (original != null) {
					locality.setAddress(original.getAddress());
				}
			}
			if (mapping.getAttributes() != null) {
				locality.setLatitude(createDecimal(mapping.getId(), mapping.getAttributes().get("LATITUDE")));
				locality.setLongitude(createDecimal(mapping.getId(), mapping.getAttributes().get("LONGITUDE")));
				locality.setTimezone(mapping.getAttributes().get("TIMEZONE"));
				locality.setDetails(mapping.getAttributes().get("DETAILS"));
				locality.setType(mapping.getAttributes().get("TYPE"));
				locality.setSubtype(mapping.getAttributes().get("SUBTYPE"));
			}
			return locality;
		}
		if (original != null) {
			original.setId(String.valueOf(mapping.getId()));
			return original;
		}
		return locality;
	}
	
	public Locality getLocality(long mappingId) {
		Mapping mapping = mappingService.getMapping(mappingId);
		if (mapping != null) {
			return createLocality(mapping, null, null);
		} else {
			return null;
		}
	}
	
	private BigDecimal createDecimal(long mappingId, String value) {
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			LOGGER.error("Invalid latitude or longitude for geo point id: " + mappingId, e);
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
