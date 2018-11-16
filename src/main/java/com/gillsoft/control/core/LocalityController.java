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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.control.service.AgregatorLocalityService;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocalityController {
	
	private static Logger LOGGER = LogManager.getLogger(LocalityController.class);
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	@Autowired
	private AgregatorLocalityService service;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private MappingService mappingService;
	
	public List<Mapping> getAll() {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_ALL, MethodType.POST);
		List<LocalityResponse> responses = service.getAll(requests);
		return getMapping(requests, responses);
	}
	
	public List<Mapping> getUsed() {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_USED, MethodType.POST);
		List<LocalityResponse> responses = service.getUsed(requests);
		return getMapping(requests, responses);
	}
	
	private List<Mapping> getMapping(List<LocalityRequest> requests, List<LocalityResponse> responses) {
		if (responses == null) {
			return new ArrayList<>(0);
		}
		// сюда складываем уникальный маппинг по каждому запрашиваемому ресурсу
		Map<Long, Mapping> idsMapping = new HashMap<>();
		for (LocalityResponse localityResponse : responses) {
			if (!Utils.isError(LOGGER, localityResponse)
					&& localityResponse.getLocalities() != null
					&& !localityResponse.getLocalities().isEmpty()) {
				Stream<LocalityRequest> stream = requests.stream().filter(request -> request.getId().equals(localityResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					LocalityRequest request = stream.findFirst().get();
					
					// мапинг ресурса
					long resourceId = request.getParams().getResource().getId();
					idsMapping.putAll(localityResponse.getLocalities().stream()
							.map(l -> mappingService.getMappings(MapType.GEO, resourceId, l.getId()))
							.flatMap(maps -> maps.stream()).collect(Collectors.toMap(Mapping::getId, m -> m)));
				}
			}
		}
		return new ArrayList<>(idsMapping.values());
	}
	
	public Map<Long, Set<Long>> getBinding() {
		List<LocalityRequest> requests = createRequest(Method.LOCALITY_BINDING, MethodType.POST);
		List<LocalityResponse> responses = service.getBinding(requests);
		if (responses == null) {
			return new HashMap<>(0);
		}
		Map<Long, Set<Long>> fromToMapping = new HashMap<>();
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
					long resourceId = request.getParams().getResource().getId();
					Map<String, Set<Long>> mapping = resourceIds.stream().collect(
							Collectors.toMap(id -> id, id -> mappingService.getMappingIds(MapType.GEO, resourceId, id)));
					
					// формируем пары маппинга from - to
					for (Entry<String, List<String>> entry : localityResponse.getBinding().entrySet()) {
						Set<Long> mappingIds = mapping.get(entry.getKey());
						if (mappingIds != null) {
							for (Long mappingId : mappingIds) {
								Set<Long> tos = fromToMapping.get(mappingId);
								if (tos == null) {
									tos = new HashSet<>();
									fromToMapping.put(mappingId, tos);
								}
								for (String id : entry.getValue()) {
									Set<Long> toMappingIds = mapping.get(id);
									if (toMappingIds != null) {
										tos.addAll(toMappingIds);
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
	
	private List<LocalityRequest> createRequest(String methodPath, MethodType methodType) {
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
			return request;
		}
		return null;
	}

}
