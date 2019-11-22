package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.control.service.AgregatorResourceInfoService;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.request.ResourceRequest;
import com.gillsoft.model.response.ResourceMethodResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResourceInfoController {
	
	private static Logger LOGGER = LogManager.getLogger(ResourceInfoController.class);
	
	private static final String ACTIVE_RESOURCES_CACHE_KEY = "methods.resource.";
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private AgregatorResourceInfoService service;
	
	public boolean isMethodAvailable(Resource resource, String methodPath, MethodType methodType) {
		List<Method> methods = getAvailableMethods(resource);
		if (methods == null) {
			return false;
		}
		return methods.stream().anyMatch(m -> m.getType() == methodType && m.getUrl().equals(methodPath));
	}
	
	@SuppressWarnings("unchecked")
	public List<Method> getAvailableMethods(Resource resource) {
		ResourceRequest request = createRequest(resource);
		
		// берем результат с кэша, если кэша нет, то берем напрямую с сервиса
		return (List<Method>) dataController.getFromCache(getActiveResourcesCacheKey(resource.getId()), new MethodsUpdateTask(request),
				() -> createMethods(request), 120000l);
	}
	
	protected List<Method> createMethods(ResourceRequest request) {
		List<ResourceMethodResponse> response = service.getAvailableMethods(Collections.singletonList(request));
		if (response == null
				|| response.isEmpty()
				|| Utils.isError(LOGGER, response.get(0))
				|| !request.getId().equals(response.get(0).getId())) {
			return new ArrayList<>(0);
		} else {
			return response.get(0).getMethods();
		}
	}
	
	// создание запроса получения информации о ресурсе 
	private ResourceRequest createRequest(Resource resource) {
		ResourceRequest request = new ResourceRequest();
		request.setId(StringUtil.generateUUID());
		request.setParams(resource.createParams());
		return request;
	}

	public static String getActiveResourcesCacheKey(long resourceId) {
		return ACTIVE_RESOURCES_CACHE_KEY + resourceId;
	}

}
