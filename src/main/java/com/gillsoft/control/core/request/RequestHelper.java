package com.gillsoft.control.core.request;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gillsoft.control.api.MethodUnavalaibleException;
import com.gillsoft.control.api.ResourceUnavailableException;
import com.gillsoft.control.core.IdModel;
import com.gillsoft.control.core.ResourceInfoController;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.service.OrderDAOManager;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;

@Component
public class RequestHelper {
	
	private static Logger LOGGER = LogManager.getLogger(RequestHelper.class);
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private OrderDAOManager manager;
	
	@Autowired
	private MsDataController dataController;
	
	public Resource getServiceResource(IdModel idModel) {
		
		// проверяем ресурс
		Resource serviceResource = dataController.getResource(idModel.getResourceId());
		if (serviceResource == null) {
			throw new ResourceUnavailableException("Resource is unavailable for service where segmentId=" + idModel.getId());
		}
		// проверяем доступность метода
		if (!infoController.isMethodAvailable(serviceResource, Method.ORDER, MethodType.POST)) {
			throw new MethodUnavalaibleException("Method is unavailable for service where segmentId=" + idModel.getId());
		}
		return serviceResource;
	}
	
	public OrderRequest createOrderRequest(OrderRequest request, Resource serviceResource) {
		OrderRequest resourceRequest = new OrderRequest();
		resourceRequest.setId(StringUtil.generateUUID());
		resourceRequest.setLang(request.getLang());
		resourceRequest.setCurrency(request.getCurrency());
		resourceRequest.setParams(serviceResource.createParams());
		resourceRequest.setServices(new ArrayList<>());
		return resourceRequest;
	}
	
	public void addUniqId(ServiceItem resourceItem, Resource serviceResource) {
		if (resourceItem.getAdditionals() == null) {
			resourceItem.setAdditionals(new HashMap<>());
		}
		try {
			resourceItem.getAdditionals().put("uniqueId", String.valueOf(manager.getUniqueId(serviceResource.getId())));
		} catch (Exception e) {
			LOGGER.error("Can not create unique id for resource " + serviceResource.getId(), e);
		}
	}

}
