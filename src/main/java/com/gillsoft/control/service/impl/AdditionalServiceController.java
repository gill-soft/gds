package com.gillsoft.control.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.control.core.IdModel;
import com.gillsoft.control.core.data.DataConverter;
import com.gillsoft.control.core.data.MsDataController;
import com.gillsoft.control.core.mapping.TripSearchMapping.SegmentAdditionalServiceMapper;
import com.gillsoft.control.core.request.SearchRequestController;
import com.gillsoft.control.service.AgregatorAdditionalSearchService;
import com.gillsoft.control.service.AgregatorOrderService;
import com.gillsoft.control.service.model.AdditionalServiceEmptyResource;
import com.gillsoft.model.AbstractJsonModel;
import com.gillsoft.model.AdditionalServiceItem;
import com.gillsoft.model.Price;
import com.gillsoft.model.RestError;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.request.AdditionalDetailsRequest;
import com.gillsoft.model.request.AdditionalSearchRequest;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.AdditionalSearchResponse;
import com.gillsoft.model.response.DocumentsResponse;
import com.gillsoft.model.response.OrderResponse;
import com.gillsoft.model.response.RequiredResponse;
import com.gillsoft.model.response.ReturnConditionResponse;
import com.gillsoft.model.response.TariffsResponse;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.ms.entity.Currency;
import com.gillsoft.ms.entity.User;
import com.gillsoft.ms.entity.ValueType;
import com.gillsoft.util.CacheUtil;
import com.gillsoft.util.StringUtil;

@Service
public class AdditionalServiceController implements AgregatorOrderService, SegmentAdditionalServiceMapper, AgregatorAdditionalSearchService {
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Override
	public OrderResponse create(OrderRequest orderRequest) {
		OrderResponse response = new OrderResponse();
		response.setCustomers(orderRequest.getCustomers());
		response.setResources(createResponse(orderRequest.getResources(), request -> createResponse(request)));
		return response;
	}
	
	private List<OrderResponse> createResponse(List<OrderRequest> requests, Function<OrderRequest, OrderResponse> creator) {
		List<OrderResponse> responses = new ArrayList<>();
		for (Iterator<OrderRequest> iterator = requests.iterator(); iterator.hasNext();) {
			OrderRequest request = iterator.next();
			if (AdditionalServiceEmptyResource.isThisId(request.getParams().getResource().getId())) {
				iterator.remove();
				responses.add(creator.apply(request));
			}
		}
		return responses;
	}
	
	private OrderResponse createResponse(OrderRequest request) {
		OrderResponse response = createBaseResponse(request);
		Map<String, AdditionalServiceItem> additionals = new HashMap<>();
		response.setAdditionalServices(additionals);
		Set<String> ids = new HashSet<>();
		for (ServiceItem serviceItem : request.getServices()) {
			response.getServices().add(serviceItem);
			if (serviceItem.getAdditionalService() != null
					&& serviceItem.getAdditionalService().getId() != null) {
				serviceItem.setId(StringUtil.generateUUID());
				ids.add(serviceItem.getId());
				if (!additionals.containsKey(serviceItem.getAdditionalService().getId())) {
					AdditionalServiceIdModel idModel = new AdditionalServiceIdModel().create(serviceItem.getAdditionalService().getId());
					com.gillsoft.ms.entity.AdditionalServiceItem additionalService = dataController.getAdditionalService(idModel.getId());
					if (additionalService == null) {
						serviceItem.setError(new RestError("Can not get additional service."));
					} else {
						additionalService.setValue(idModel.getValue());
						additionalService.setCurrency(idModel.getCurrency());
						AdditionalServiceItem additionalServiceItem = convert(additionalService);
						additionals.put(additionalServiceItem.getId(), additionalServiceItem);
					}
				}
				AdditionalServiceItem additionalServiceItem = additionals.get(serviceItem.getAdditionalService().getId());
				serviceItem.setPrice((Price) SerializationUtils.deserialize(SerializationUtils.serialize(additionalServiceItem.getPrice())));
			}
		}
		response.setOrderId(new OrderIdModel(ids).asString());
		return response;
	}
	
	private OrderResponse createBaseResponse(OrderRequest request) {
		OrderResponse response = new OrderResponse();
		response.setId(request.getId());
		response.setOrderId(request.getOrderId());
		response.setServices(new ArrayList<>());
		return response;
	}
	
	@Override
	public List<OrderResponse> confirm(List<OrderRequest> requests) {
		return createResponse(requests, request -> confirmResponse(request));
	}
	
	private OrderResponse confirmResponse(OrderRequest request) {
		OrderResponse response = createBaseResponse(request);
		OrderIdModel idModel = new OrderIdModel().create(request.getOrderId());
		for (String id : idModel.getIds()) {
			ServiceItem serviceItem = new ServiceItem(id);
			serviceItem.setConfirmed(true);
			response.getServices().add(serviceItem);
		}
		return response;
	}
	
	public AdditionalServiceItem convert(com.gillsoft.ms.entity.AdditionalServiceItem additionalService) {
		AdditionalServiceItem additionalServiceItem = DataConverter.convert(additionalService);
		additionalServiceItem.setId(new AdditionalServiceIdModel(additionalServiceItem).asString());
		return additionalServiceItem;
	}
	
	@Override
	public List<OrderResponse> addServices(List<OrderRequest> request) {
		return null;
	}

	@Override
	public List<OrderResponse> removeServices(List<OrderRequest> request) {
		return null;
	}

	@Override
	public List<OrderResponse> updateCustomers(List<OrderRequest> request) {
		return null;
	}

	@Override
	public List<OrderResponse> get(List<OrderRequest> request) {
		return null;
	}

	@Override
	public List<OrderResponse> getService(List<OrderRequest> request) {
		return null;
	}

	@Override
	public List<OrderResponse> booking(List<OrderRequest> request) {
		return null;
	}

	@Override
	public List<OrderResponse> cancel(List<OrderRequest> requests) {
		return createResponse(requests, request -> confirmResponse(request));
	}

	@Override
	public List<OrderResponse> prepareReturnServices(List<OrderRequest> requests) {
		return createResponse(requests, request -> confirmResponse(request));
	}

	@Override
	public List<OrderResponse> returnServices(List<OrderRequest> requests) {
		return createResponse(requests, request -> confirmResponse(request));
	}

	@Override
	public List<OrderResponse> getPdfDocuments(List<OrderRequest> request) {
		return null;
	}

	@Override
	public void addServices(TripSearchRequest request, TripSearchResponse result, Segment segment) {
		List<com.gillsoft.ms.entity.AdditionalServiceItem> additionalServices = dataController.getAdditionalServices(segment);
		if (additionalServices != null) {
			if (segment.getAdditionalServices() == null) {
				segment.setAdditionalServices(new ArrayList<>());
			}
			additionalServices = additionalServices.stream().filter(com.gillsoft.ms.entity.AdditionalServiceItem::isUsedWithTrip).collect(Collectors.toList());
			for (com.gillsoft.ms.entity.AdditionalServiceItem additionalServiceEntity : additionalServices) {
				if (additionalServiceEntity.getValueType() == ValueType.PERCENT) {
					additionalServiceEntity.setValue(segment.getPrice().getTariff().getValue()
							.multiply(additionalServiceEntity.getValue().multiply(new BigDecimal(0.01))));
					additionalServiceEntity.setCurrency(Currency.valueOf(segment.getPrice().getCurrency().name()));
				}
				AdditionalServiceItem additionalService = convert(additionalServiceEntity);
				additionalService.setPrice(dataController.recalculate(additionalService.getPrice(), request.getCurrency()));
				additionalService.setId(new IdModel(new AdditionalServiceEmptyResource().getId(), additionalService.getId()).asString());
				segment.getAdditionalServices().add(additionalService);
				DataConverter.applyLang(additionalService, request.getLang());
				result.getAdditionalServices().put(additionalService.getId(), additionalService);
			}
		}
	}

	public static class AdditionalServiceIdModel extends IdModel {

		private static final long serialVersionUID = -4125735464064216044L;
		private BigDecimal value;
		private Currency currency;
		private boolean returnEnabled;

		public AdditionalServiceIdModel() {
			super();
		}

		public AdditionalServiceIdModel(AdditionalServiceItem additionalService) {
			setId(additionalService.getId());
			setValue(additionalService.getPrice().getAmount());
			setCurrency(Currency.valueOf(additionalService.getPrice().getCurrency().name()));
			setReturnEnabled(additionalService.getEnableReturn() == null ? true : additionalService.getEnableReturn());
		}

		public BigDecimal getValue() {
			return value;
		}

		public void setValue(BigDecimal value) {
			this.value = value;
		}

		public Currency getCurrency() {
			return currency;
		}

		public void setCurrency(Currency currency) {
			this.currency = currency;
		}

		public boolean isReturnEnabled() {
			return returnEnabled;
		}

		public void setReturnEnabled(boolean returnEnabled) {
			this.returnEnabled = returnEnabled;
		}

		@Override
		public AdditionalServiceIdModel create(String json) {
			return (AdditionalServiceIdModel) super.create(json);
		}

	}
	
	public static class OrderIdModel extends AbstractJsonModel {

		private static final long serialVersionUID = 8727827155035694863L;
		
		private Set<String> ids;

		public OrderIdModel() {
			super();
		}

		public OrderIdModel(Set<String> ids) {
			super();
			this.ids = ids;
		}

		public Set<String> getIds() {
			return ids;
		}

		public void setIds(Set<String> ids) {
			this.ids = ids;
		}
		
		@Override
		public OrderIdModel create(String json) {
			return (OrderIdModel) super.create(json);
		}
		
	}

	@Override
	public AdditionalSearchResponse initSearch(List<AdditionalSearchRequest> requests) {
		AdditionalSearchResponse response = new AdditionalSearchResponse();
		for (AdditionalSearchRequest request : requests) {
			if (request.getOrder() != null
					&& request.getOrder().getSegments() != null) {
				
				signIn(request);
				
				// получаем допуслуги на сегменты заказа
				for (Entry<String, Segment> segmentEntry : request.getOrder().getSegments().entrySet()) {
					List<com.gillsoft.ms.entity.AdditionalServiceItem> additionalServices = dataController.getAdditionalServices(segmentEntry.getValue());
					if (additionalServices != null) {
						additionalServices = additionalServices.stream().filter(s -> !s.isUsedWithTrip()).collect(Collectors.toList());
						
						// получаем список сервисов, к которым применяется допуслуга
						List<ServiceItem> services = getSegmentServices(request.getOrder(), segmentEntry.getKey());
						BigDecimal orderAmount = getServicesAmount(services);
						Currency currency = getServicesCurrency(services);
						
						AdditionalSearchResponse requestResponse = new AdditionalSearchResponse();
						requestResponse.setId(request.getId());
						Map<String, AdditionalServiceItem> additionals = new HashMap<>();
						for (com.gillsoft.ms.entity.AdditionalServiceItem additionalServiceEntity : additionalServices) {
							if (additionalServiceEntity.getValueType() == ValueType.PERCENT) {
								additionalServiceEntity.setValue(orderAmount
										.multiply(additionalServiceEntity.getValue().multiply(new BigDecimal(0.01))));
								additionalServiceEntity.setCurrency(currency);
							}
							AdditionalServiceItem additionalService = convert(additionalServiceEntity);
							additionalService.setId(String.valueOf(additionalServiceEntity.getId()));
							DataConverter.applyLang(additionalService, request.getLang());
							AdditionalServiceItem present = additionals.get(additionalService.getId());
							if (present != null) {
								joinServices(present, additionalService);
							} else {
								additionals.put(additionalService.getId(), additionalService);
							}
						}
						requestResponse.setAdditionalServices(new HashMap<>());
						for (AdditionalServiceItem additionalServiceItem : additionals.values()) {
							additionalServiceItem.setId(new AdditionalServiceIdModel(additionalServiceItem).asString());
							requestResponse.getAdditionalServices().put(additionalServiceItem.getId(), additionalServiceItem);
						}
						if (response.getResult() == null) {
							response.setResult(new ArrayList<>());
						}
						response.getResult().add(requestResponse);
					}
				}
			}
		}
		String cacheId = StringUtil.generateUUID();
		CacheUtil.putToCache(cache, cacheId, response);
		return new AdditionalSearchResponse(null, cacheId);
	}
	
	public void signIn(AdditionalSearchRequest request) {
		if (dataController.getUserName() != null
				|| request.getParams() == null
				|| request.getParams().getAdditional() == null
				|| !request.getParams().getAdditional().containsKey(SearchRequestController.USER_ID_KEY)) {
			return;
		}
		String userId = request.getParams().getAdditional().get(SearchRequestController.USER_ID_KEY);
		User user = dataController.getUser(Long.parseLong(userId));
		if (user != null) {
			Authentication auth = new UsernamePasswordAuthenticationToken(user.getLogin(), user.getPassword());
			auth = authenticationManager.authenticate(auth);
			if (!auth.isAuthenticated()) {
				throw new AuthenticationServiceException("Not authorized");
			}
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
	}
	
	private void joinServices(AdditionalServiceItem present, AdditionalServiceItem additionalService) {
		present.getSegments().addAll(additionalService.getSegments());
		present.getServices().addAll(additionalService.getServices());
		present.getPrice().setAmount(present.getPrice().getAmount().add(additionalService.getPrice().getAmount()));
		Tariff presentTariff = present.getPrice().getTariff();
		Tariff tariff = additionalService.getPrice().getTariff();
		presentTariff.setValue(presentTariff.getValue().add(tariff.getValue()));
		presentTariff.setVat(presentTariff.getVat().add(tariff.getVat()));
	}
	
	private List<ServiceItem> getSegmentServices(OrderResponse order, String segmentId) {
		return order.getServices().stream().filter(s -> s.getSegment() != null && segmentId.equals(s.getSegment().getId())).collect(Collectors.toList());
	}
	
	private BigDecimal getServicesAmount(List<ServiceItem> services) {
		BigDecimal total = BigDecimal.ZERO;
		for (ServiceItem item : services) {
			if (item.getPrice() != null) {
				total = total.add(item.getPrice().getAmount());
			}
		}
		return total;
	}
	
	private Currency getServicesCurrency(List<ServiceItem> services) {
		return Currency.valueOf(services.get(0).getPrice().getCurrency().name());
	}

	@Override
	public AdditionalSearchResponse getSearchResult(String searchId) {
		return (AdditionalSearchResponse) CacheUtil.getFromCache(cache, searchId);
	}

	@Override
	public List<TariffsResponse> getTariffs(List<AdditionalDetailsRequest> requests) {
		return null;
	}

	@Override
	public List<RequiredResponse> getRequiredFields(List<AdditionalDetailsRequest> requests) {
		return null;
	}

	@Override
	public List<ReturnConditionResponse> getConditions(List<AdditionalDetailsRequest> requests) {
		return null;
	}

	@Override
	public List<DocumentsResponse> getDocuments(List<AdditionalDetailsRequest> requests) {
		return null;
	}
	
}
