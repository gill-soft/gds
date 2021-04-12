package com.gillsoft.control.service.impl;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SerializationUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.gillsoft.control.config.Config;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.model.ResponseError;
import com.gillsoft.ms.entity.AdditionalServiceItem;
import com.gillsoft.ms.entity.Attribute;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.BaseEntityDeserializer;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.ConnectionDiscount;
import com.gillsoft.ms.entity.OrderAccess;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ResourceConnection;
import com.gillsoft.ms.entity.ResourceFilter;
import com.gillsoft.ms.entity.ResourceParams;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.ServiceFilter;
import com.gillsoft.ms.entity.TariffMarkup;
import com.gillsoft.ms.entity.TicketLayout;
import com.gillsoft.ms.entity.Trip;
import com.gillsoft.ms.entity.User;

@Service
public class MsDataRestService extends AbstractRestService implements MsDataService {
	
	private static Logger LOGGER = LogManager.getLogger(MsDataRestService.class);
	
	private static final String ALL_COMMISSIONS = "commission/all_with_parents";
	
	private static final String ALL_RETURN_CONDITIONS = "condition/all_with_parents";
	
	private static final String ALL_TICKET_LAYOUTS = "ticket_layout/all_with_parents";
	
	private static final String ALL_FILTERS = "filter/all_with_parents";
	
	private static final String ALL_ORDERS_ACCESS = "order_access/all_with_sub_main";
	
	private static final String ALL_RESOURCE_FILTERS = "resource_filter/all_with_sub_main";
	
	private static final String ALL_RESOURCE_CONNECTIONS = "resource_connection/all_with_sub_main";
	
	private static final String ALL_RESOURCE_CONNECTION_DISCOUNTS = "connection_discount/all_with_sub_main";
	
	private static final String ALL_TARIFF_MARKUPS = "tariff_markup/all_with_sub_main";
	
	private static final String ALL_ADDITIONAL_SERVICES = "additional_service/all_with_sub_main";
	
	private static final String GET_USER_BY_NAME = "user/by_name_with_parents/{0}";
	
	private static final String GET_USER = "user/{0}";
	
	private static final String GET_USER_ORGANISATION = "user/{0}/organisation";
	
	private static final String GET_USER_RESOURCES = "user/{0}/resources";
	
	private static final String GET_ORGANISATION = "organisation/{0}";
	
	private static final String GET_ADDITIONAL_SERVICE = "additional_service/{0}";
	
	private static final String GET_TRIP = "trip/{0}";
	
	private static final String GET_TRIP_PARENT = "trip/{0}/parent";
	
	private static final String GET_TRIP_CHILDREN = "trip/{0}/sub";
	
	private static final String ALL_ORGANISATIONS = "organisation";
	
	private static final String ALL_RESOURCE_PARAMS_WITH_PARENTS = "resource_params/all_with_parents";
	
	private static final String ALL_ATTRIBUTES = "attribute";
	
	private RestTemplate template;
	
	@Autowired
	@Qualifier("msAuthHeader")
	private HttpHeaders msAuth;
	
	@Bean(name = "msAuthHeader")
	public HttpHeaders createMsAuthHeaders() {
		HttpHeaders headers = new HttpHeaders();
		String auth = Config.getMsLogin() + ":" + Config.getMsPassword();
		byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
		String authHeader = "Basic " + new String(encodedAuth);
		headers.add(HttpHeaders.AUTHORIZATION, authHeader);
		return headers;
	}

	@Override
	public List<Resource> getUserResources(String userName) {
		return getResultByUser(userName, GET_USER_RESOURCES, new ParameterizedTypeReference<List<Resource>>() { });
	}

	@Override
	public User getUser(String userName) {
		return getResultByUser(userName, GET_USER_BY_NAME, new ParameterizedTypeReference<User>() { });
	}
	
	@Override
	public User getUser(long id) {
		User user = getResultByUser(String.valueOf(id), GET_USER, new ParameterizedTypeReference<User>() { });
		if (user != null) {
			return getUser(user.getLogin());
		}
		return null;
	}
	
	@Override
	public Organisation getUserOrganisation(String userName) {
		return getResultByUser(userName, GET_USER_ORGANISATION, new ParameterizedTypeReference<Organisation>() { });
	}
	
	@Override
	public Organisation getOrganisation(long id) {
		return getResult(MessageFormat.format(GET_ORGANISATION, String.valueOf(id)), null, new ParameterizedTypeReference<Organisation>() { });
	}
	
	@Override
	public List<Organisation> getAllOrganisations() {
		return getResult(ALL_ORGANISATIONS, null, new ParameterizedTypeReference<List<Organisation>>() { });
	}

	@Override
	public List<Commission> getAllCommissions() {
		return getResult(ALL_COMMISSIONS, null, new ParameterizedTypeReference<List<Commission>>() { });
	}
	
	private <T> T getResultByUser(String userIdentifier, String method, ParameterizedTypeReference<T> type) {
		return getResult(MessageFormat.format(method, userIdentifier), null, type);
	}
	
	private <T> T getResult(String method, MultiValueMap<String, String> params, ParameterizedTypeReference<T> type) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(Config.getMsUrl() + method);
		if (params != null) {
			builder.queryParams(params);
		}
		URI uri = builder.build().toUri();
		RequestEntity<Object> entity = new RequestEntity<Object>(msAuth, HttpMethod.GET, uri);
		try {
			return getResult(entity, type);
		} catch (ResponseError e) {
			return null;
		}
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public RestTemplate getTemplate() {
		if (template == null) {
			synchronized (MsDataRestService.class) {
				if (template == null) {
					template = createTemplate(Config.getMsUrl());
					for (HttpMessageConverter<?> converter : template.getMessageConverters()) {
						if (converter instanceof AbstractJackson2HttpMessageConverter) {
							SimpleModule module = new SimpleModule();
							module.setDeserializerModifier(new BeanDeserializerModifier() {
								@Override
								public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
										JsonDeserializer<?> deserializer) {
									if (beanDesc.getBeanClass() == BaseEntity.class) {
										return new BaseEntityDeserializer(deserializer);
									}
									return deserializer;
								}
							});
							((AbstractJackson2HttpMessageConverter) converter).getObjectMapper()
									.registerModule(module);
						}
					}
				}
			}
		}
		return template;
	}

	@Override
	public List<ReturnCondition> getAllReturnConditions() {
		return getResult(ALL_RETURN_CONDITIONS, null, new ParameterizedTypeReference<List<ReturnCondition>>() { });
	}
	
	@Override
	public List<TicketLayout> getAllTicketLayouts() {
		return getResult(ALL_TICKET_LAYOUTS, null, new ParameterizedTypeReference<List<TicketLayout>>() { });
	}

	@Override
	public List<ServiceFilter> getAllFilters() {
		return getResult(ALL_FILTERS, null, new ParameterizedTypeReference<List<ServiceFilter>>() { });
	}

	@Override
	public List<OrderAccess> getAllOrdersAccess() {
		return getResult(ALL_ORDERS_ACCESS, null, new ParameterizedTypeReference<List<OrderAccess>>() { });
	}
	
	@Override
	public List<ResourceFilter> getAllResourceFilters() {
		List<ResourceFilter> resourceFilters =
				getResult(ALL_RESOURCE_FILTERS, null, new ParameterizedTypeReference<List<ResourceFilter>>() { });
		return new EntityMultiplier<ResourceFilter>().multiplyChilds(resourceFilters);
	}

	@Override
	public List<ResourceConnection> getAllResourceConnections() {
		List<ResourceConnection> resourceConnections =
				getResult(ALL_RESOURCE_CONNECTIONS, null, new ParameterizedTypeReference<List<ResourceConnection>>() { });
		return new EntityMultiplier<ResourceConnection>().multiplyChilds(resourceConnections);
	}

	@Override
	public List<ConnectionDiscount> getAllResourceConnectionDiscounts() {
		List<ConnectionDiscount> discounts =
				getResult(ALL_RESOURCE_CONNECTION_DISCOUNTS, null, new ParameterizedTypeReference<List<ConnectionDiscount>>() { });
		return new EntityMultiplier<ConnectionDiscount>().multiplyChilds(discounts);
	}
	
	@Override
	public List<TariffMarkup> getAllTariffMarkups() {
		return getResult(ALL_TARIFF_MARKUPS, null, new ParameterizedTypeReference<List<TariffMarkup>>() { });
	}
	
	@Override
	public List<AdditionalServiceItem> getAllAdditionalServices() {
		return getResult(ALL_ADDITIONAL_SERVICES, null, new ParameterizedTypeReference<List<AdditionalServiceItem>>() { });
	}
	
	@Override
	public List<ResourceParams> getAllResourceParamsWithParent() {
		return getResult(ALL_RESOURCE_PARAMS_WITH_PARENTS, null, new ParameterizedTypeReference<List<ResourceParams>>() { });
	}
	
	@Override
	public List<Attribute> getAllAttributes() {
		return getResult(ALL_ATTRIBUTES, null, new ParameterizedTypeReference<List<Attribute>>() { });
	}
	
	@Override
	public Trip getTripWithParentsChilds(long id) {
		Trip trip = getResult(MessageFormat.format(GET_TRIP, String.valueOf(id)), null, new ParameterizedTypeReference<Trip>() { });
		if (trip != null) {
			trip.setParents(getResult(MessageFormat.format(GET_TRIP_PARENT, String.valueOf(id)), null, new ParameterizedTypeReference<Set<BaseEntity>>() { }));
			trip.setChilds(getResult(MessageFormat.format(GET_TRIP_CHILDREN, String.valueOf(id)), null, new ParameterizedTypeReference<Set<BaseEntity>>() { }));
		}
		return trip;
	}
	
	@Override
	public AdditionalServiceItem getAdditionalService(long id) {
		return getResult(MessageFormat.format(GET_ADDITIONAL_SERVICE, String.valueOf(id)), null, new ParameterizedTypeReference<AdditionalServiceItem>() { });
	}
	
	private class EntityMultiplier<T extends BaseEntity> {
		
		@SuppressWarnings("unchecked")
		public List<T> multiplyChilds(List<T> entities) {
			if (entities == null) {
				return null;
			}
			// размножаем сущность на каждого родителя так как она применяется к ресурсам,
			// а назначается на организацию и пользователя
			List<T> newEntities = new ArrayList<>();
			long i = -1;
			for (T entity : entities) {
				if (entity.getParents() != null) {
					if (entity.getParents().size() > 1) {
						for (BaseEntity parent : entity.getParents()) {
							T newEntity = (T) SerializationUtils.deserialize(
									SerializationUtils.serialize(entity));
							newEntity.setId(i--);
							newEntity.setParents(Collections.singleton(parent));
							newEntities.add(newEntity);
						}
					} else {
						newEntities.add(entity);
					}
				}
			}
			return newEntities;
		}
		
	}

}
