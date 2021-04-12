package com.gillsoft.control.core.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.control.core.mapping.TripIdModel;
import com.gillsoft.control.core.request.OrderRequestController.ServiceOrderRequestHandler;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.ms.entity.Resource;

@Component
public class SegmentRequest implements ServiceOrderRequestHandler {
	
	@Autowired
	private RequestHelper requestHelper;

	@Override
	public List<OrderRequest> create(OrderRequest request) {
		Map<Long, OrderRequest> requests = new HashMap<>();
		for (ServiceItem item : request.getServices()) {
			if (item.getSegment() != null) {
				ServiceItem resourceItem = (ServiceItem) SerializationUtils.deserialize(SerializationUtils.serialize(item));
				TripIdModel idModel = new TripIdModel().create(resourceItem.getSegment().getId());
				
				// проверяем ресурс
				Resource serviceResource = requestHelper.getServiceResource(idModel);
				OrderRequest resourceRequest = requests.get(serviceResource.getId());
				if (resourceRequest == null) {
					resourceRequest = requestHelper.createOrderRequest(request, serviceResource);
					requests.put(serviceResource.getId(), resourceRequest);
				}
				resourceItem.getSegment().setId(idModel.getId());
				if (resourceItem.getCarriage() != null) {
					resourceItem.getCarriage().setId(new TripIdModel().create(resourceItem.getCarriage().getId()).getId());
				}
				requestHelper.addUniqId(resourceItem, serviceResource);
				resourceRequest.getServices().add(resourceItem);
			}
		}
		return new ArrayList<>(requests.values());
	}

}
