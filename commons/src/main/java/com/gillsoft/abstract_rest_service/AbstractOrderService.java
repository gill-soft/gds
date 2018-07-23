package com.gillsoft.abstract_rest_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.gillsoft.model.Method;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.model.service.OrderService;

public abstract class AbstractOrderService implements OrderService {

	@Override
	@PostMapping(Method.ORDER)
	public OrderResponse create(@RequestBody OrderRequest request) {
		OrderResponse response = createResponse(request);
		response.fillMaps();
		return response;
	}
	
	public abstract OrderResponse createResponse(OrderRequest request);

	@Override
	@PostMapping(Method.ORDER_SERVICE_ADD)
	public OrderResponse addServices(@RequestBody OrderRequest request) {
		return addServicesResponse(request);
	}
	
	public abstract OrderResponse addServicesResponse(OrderRequest request);

	@Override
	@PostMapping(Method.ORDER_SERVICE_REMOVE)
	public OrderResponse removeServices(@RequestBody OrderRequest request) {
		return removeServicesResponse(request);
	}
	
	public abstract OrderResponse removeServicesResponse(OrderRequest request);

	@Override
	@PostMapping(Method.ORDER_CUSTOMER_UPDATE)
	public OrderResponse updateCustomers(@RequestBody OrderRequest request) {
		return updateCustomersResponse(request);
	}
	
	public abstract OrderResponse updateCustomersResponse(OrderRequest request);

	@Override
	@GetMapping(Method.ORDER)
	public OrderResponse get(@RequestParam("orderId") String orderId) {
		return getResponse(orderId);
	}
	
	public abstract OrderResponse getResponse(String orderId);
	
	@Override
	@GetMapping(Method.ORDER_SERVICE)
	public OrderResponse getService(@RequestParam("serviceId") String serviceId) {
		return getServiceResponse(serviceId);
	}
	
	public abstract OrderResponse getServiceResponse(String serviceId);

	@Override
	@PostMapping(Method.ORDER_BOOKING)
	public OrderResponse booking(@RequestParam("orderId") String orderId) {
		return bookingResponse(orderId);
	}
	
	public abstract OrderResponse bookingResponse(String orderId);

	@Override
	@PostMapping(Method.ORDER_CONFIRM)
	public OrderResponse confirm(@RequestParam("orderId") String orderId) {
		return confirmResponse(orderId);
	}
	
	public abstract OrderResponse confirmResponse(String orderId);

	@Override
	@PostMapping(Method.ORDER_CANCEL)
	public OrderResponse cancel(@RequestParam("orderId") String orderId) {
		return cancelResponse(orderId);
	}
	
	public abstract OrderResponse cancelResponse(String orderId);

	@Override
	@PostMapping(Method.ORDER_RETURN_PREPARE)
	public OrderResponse prepareReturnServices(@RequestBody OrderRequest request) {
		return prepareReturnServicesResponse(request);
	}
	
	public abstract OrderResponse prepareReturnServicesResponse(OrderRequest request);
	
	@Override
	@PostMapping(Method.ORDER_RETURN_CONFIRM)
	public OrderResponse returnServices(@RequestBody OrderRequest request) {
		return returnServicesResponse(request);
	}
	
	public abstract OrderResponse returnServicesResponse(OrderRequest request);

	@Override
	@PostMapping(Method.ORDER_DOCUMENTS)
	public OrderResponse getPdfDocuments(@RequestBody OrderRequest request) {
		return getPdfDocumentsResponse(request);
	}
	
	public abstract OrderResponse getPdfDocumentsResponse(OrderRequest request);

}
