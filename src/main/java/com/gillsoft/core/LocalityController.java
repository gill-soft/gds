package com.gillsoft.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.entity.Resource;
import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.request.LocalityRequest;
import com.gillsoft.model.response.LocalityResponse;
import com.gillsoft.service.AgregatorLocalityService;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocalityController {
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	@Autowired
	private AgregatorLocalityService service;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private MappingService mappingService;
	
	public List<Mapping> getAll() {
		
		// сюда складываем уникальный маппинг по каждому запрашиваемому ресурсу
		Map<Long, Mapping> idsMapping = new HashMap<>();
		List<LocalityRequest> requests = createRequest();
		List<LocalityResponse> responses = service.getAll(createRequest());
		for (LocalityResponse localityResponse : responses) {
			if (localityResponse.getLocalities() != null
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
	
	public List<Mapping> getUsed() {
		//TODO
		return null;
	}
	
	public Map<Long, List<Long>> getBinding() {
		return null;
	}
	
	private List<LocalityRequest> createRequest() {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			List<LocalityRequest> request = new ArrayList<>();
			for (Resource resource : resources) {
				LocalityRequest localityRequest = new LocalityRequest();
				localityRequest.setId(StringUtil.generateUUID());
				localityRequest.setParams(resource.createParams());
				request.add(localityRequest);
			}
			return request;
		}
		return null;
	}

}
