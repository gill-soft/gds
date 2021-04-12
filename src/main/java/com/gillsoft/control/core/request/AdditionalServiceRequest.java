package com.gillsoft.control.core.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.core.IdModel;
import com.gillsoft.control.core.request.OrderRequestController.ServiceOrderRequestHandler;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.ms.entity.Resource;

@Component
public class AdditionalServiceRequest implements ServiceOrderRequestHandler {
	
	@Autowired
	private RequestHelper requestHelper;

	@Override
	public List<OrderRequest> create(OrderRequest request) {
		Map<Long, OrderRequest> requests = new HashMap<>();
		for (ServiceItem item : request.getServices()) {
			if (item.getAdditionalService() != null) {
				ServiceItem resourceItem = (ServiceItem) SerializationUtils.deserialize(SerializationUtils.serialize(item));
				IdModel idModel = new IdModel().create(resourceItem.getAdditionalService().getId());
				
				// проверяем ресурс
				Resource serviceResource = requestHelper.getServiceResource(idModel);
				OrderRequest resourceRequest = requests.get(serviceResource.getId());
				if (resourceRequest == null) {
					resourceRequest = requestHelper.createOrderRequest(request, serviceResource);
					requests.put(serviceResource.getId(), resourceRequest);
				}
				resourceItem.getAdditionalService().setId(idModel.getId());
				requestHelper.addUniqId(resourceItem, serviceResource);
				resourceRequest.getServices().add(resourceItem);
			}
		}
		return new ArrayList<>(requests.values());
	}

}
