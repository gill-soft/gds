package com.gillsoft.control.core.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.commission.Calculator;
import com.gillsoft.control.core.Utils;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.control.service.model.AdditionalServiceEmptyResource;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Price;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.ms.entity.AdditionalServiceItem;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.CodeEntity;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.ConnectionDiscount;
import com.gillsoft.ms.entity.EntityType;
import com.gillsoft.ms.entity.OrderAccess;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ResourceConnection;
import com.gillsoft.ms.entity.ResourceFilter;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.ServiceFilter;
import com.gillsoft.ms.entity.TariffMarkup;
import com.gillsoft.ms.entity.TicketLayout;
import com.gillsoft.ms.entity.Trip;
import com.gillsoft.ms.entity.User;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MsDataController {
	
	private static Logger LOGGER = LogManager.getLogger(MsDataController.class);
	
	private static final String ACTIVE_RESOURCES_CACHE_KEY = "active.resources.";
	
	private static final String ALL_COMMISSIONS_KEY = "all.commissions";
	
	private static final String ALL_TARIFF_MARKUPS_KEY = "all.tariff.markups";
	
	private static final String ALL_ADDITIONAL_SERVICES_KEY = "all.additional.services";
	
	private static final String ALL_RETURN_CONDITIONS_KEY = "all.return.conditions";
	
	private static final String ALL_TICKET_LAYOUTS_KEY = "all.ticket.layouts";
	
	private static final String ALL_FILTERS_KEY = "all.filters";
	
	private static final String ALL_ORDERS_ACCESS_KEY = "all.orders.access";
	
	private static final String ALL_RESOURCE_FILTERS_KEY = "all.resource.filters";
	
	private static final String ALL_RESOURCE_CONNECTIONS_KEY = "all.resource.connections";
	
	private static final String ALL_RESOURCE_CONNECTION_DISCOUNTS_KEY = "all.resource.connection.discounts";
	
	private static final String ALL_ORGANISATIONS_KEY = "all.organisations";
	
	private static final String ALL_RESOURCE_ORDERS_PARAMS_KEY = "all.resource.orders.params";
	
	private static final String USER_KEY = "user.";
	
	private static final String USER_ORGANISATION_KEY = "organisation.";
	
	private static final String TRIP_KEY = "trip.";
	
	private static final String ADDITIONAL_SERVICE_KEY = "additional.service.";
	
	@Autowired
	private MsDataService msService;
	
	@Autowired
    @Qualifier("MemoryCacheHandler")
	private CacheHandler cache;
	
	@Autowired
	private Calculator calculator;
	
	public String getUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		return authentication.getName();
	}
	
	public List<Resource> getUserResources() {
		String userName = getUserName();
		return getUserResources(userName);
	}
	
	@SuppressWarnings("unchecked")
	private List<Resource> getUserResources(String userName) {
		return (List<Resource>) getFromCache(getActiveResourcesCacheKey(userName),
				new UserResourcesUpdateTask(userName), () -> new CopyOnWriteArrayList<>(msService.getUserResources(userName)), 120000l);
	}
	
	public Resource getResource(long resourceId) {
		if (AdditionalServiceEmptyResource.isThisId(resourceId)) {
			return new AdditionalServiceEmptyResource();
		}
		return getResource(String.valueOf(resourceId));
	}
	
	public Resource getResource(String resourceId) {
		if (AdditionalServiceEmptyResource.isThisId(resourceId)) {
			return new AdditionalServiceEmptyResource();
		}
		List<Resource> resources = getUserResources();
		Optional<Resource> resource = resources.stream().filter(r -> String.valueOf(r.getId()).equals(resourceId)).findFirst();
		if (resource.isPresent()) {
			return resource.get();
		} else {
			return null;
		}
	}
	
	public ResourceParams createResourceParams(long resourceId) {
		return createResourceParams(resourceId, getUserName());
	}
	
	public ResourceParams createResourceParams(long resourceId, String userName) {
		List<Resource> resources = getUserResources(userName);
		if (resources != null) {
			for (Resource resource : resources) {
				if (resource.getId() == resourceId) {
					return resource.createParams();
				}
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllCommissions() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllCommissionsKey(),
				new AllCommissionsUpdateTask(), () -> toMap(msService.getAllCommissions()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllTariffMarkups() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllTariffMarkupsKey(),
				new AllTariffMarkupsUpdateTask(), () -> toMap(msService.getAllTariffMarkups()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllAdditionalServices() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllAdditionalServicesKey(),
				new AllAdditionalServicesUpdateTask(), () -> toMap(msService.getAllAdditionalServices()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllReturnConditions() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllReturnConditionsKey(),
				new AllReturnConditionsUpdateTask(), () -> toMap(msService.getAllReturnConditions()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllTicketLayouts() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllTicketLayoutsKey(),
				new AllTicketLayoutsUpdateTask(), () -> toMap(msService.getAllTicketLayouts()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllFilters() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllFiltersKey(),
				new AllFiltersUpdateTask(), () -> toMap(msService.getAllFilters()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllOrdersAccess() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllOrdersAccessKey(),
				new AllOrdersAccessUpdateTask(), () -> toMap(msService.getAllOrdersAccess()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllResourceFilters() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllResourceFiltersKey(),
				new AllResourceFiltersUpdateTask(), () -> toMap(msService.getAllResourceFilters()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllResourceConnections() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllResourceConnectionsKey(),
				new AllResourceConnectionsUpdateTask(), () -> toMap(msService.getAllResourceConnections()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Long, List<CodeEntity>> getAllResourceConnectionDiscounts() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllResourceConnectionDiscountsKey(),
				new AllConnectionDiscountsUpdateTask(), () -> toMap(msService.getAllResourceConnectionDiscounts()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Organisation> getAllOrganisations() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<String, Organisation>) getFromCache(getAllOrganisationsCacheKey(),
				new AllOrganisationsUpdateTask(), () -> createOrganisationsMap(msService.getAllOrganisations()), 120000l);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Organisation> createOrganisationsMap(List<Organisation> entities) {
		if (entities != null) {
			Map<String, Organisation> result = (Map<String, Organisation>) createEntitiesMap(entities);
			result.putAll(entities.stream().filter(o -> o.getMappingId() != 0).collect(Collectors.toMap(o -> getMappingId(o.getMappingId()), o -> o, (o1, o2) -> o1)));
			return result;
		} else {
			return new HashMap<>(0);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, com.gillsoft.ms.entity.ResourceParams> getAllResourceParams() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<String, com.gillsoft.ms.entity.ResourceParams>) getFromCache(getAllResourceParamsCacheKey(),
				new AllResourceParamsUpdateTask(), () -> createEntitiesMap(msService.getAllResourceParamsWithParent()), 120000l);
	}
	
	public Map<String, ? extends BaseEntity> createEntitiesMap(List<? extends BaseEntity> entities) {
		if (entities != null) {
			return entities.stream().collect(Collectors.toMap(o -> getEntityId(o.getId()), o -> o, (o1, o2) -> o1));
		} else {
			return new HashMap<>(0);
		}
	}
	
	private String getEntityId(long id) {
		return "entity_id_" + id;
	}
	
	private String getMappingId(long id) {
		return "mapping_id_" + id;
	}
	
	public Map<Long, List<BaseEntity>> toMap(List<? extends BaseEntity> entities) {
		if (entities != null) {
			
			// entity id -> list of entities
			Map<Long, List<BaseEntity>> grouping = new ConcurrentHashMap<>();
			entities.forEach(entity -> {
				if (entity.getParents() != null) {
					entity.setParents(new CopyOnWriteArraySet<>(entity.getParents()));
					for (BaseEntity parent : entity.getParents()) {
						List<BaseEntity> groupe = grouping.get(parent.getId());
						if (groupe == null) {
							groupe = new CopyOnWriteArrayList<>();
							grouping.put(parent.getId(), groupe);
						}
						groupe.add(entity);
					}
				}
			});
			return grouping;
		} else {
			return new HashMap<>(0);
		}
	}
	
	public User getUser() {
		String userName = getUserName();
		return (User) getFromCache(getUserCacheKey(userName),
				new UserByNameUpdateTask(userName), () -> msService.getUser(userName), 120000l);
	}
	
	public Organisation getUserOrganisation() {
		String userName = getUserName();
		return (Organisation) getFromCache(getUserOrganisationCacheKey(userName),
				new UserOrganisationUpdateTask(userName), () -> msService.getUserOrganisation(userName), 120000l);
	}
	
	public String getUserTimeZone() {
		if (getUser().getParents() != null) {
			BaseEntity organisation = getUser().getParents().iterator().next();
			if (organisation.getAttributeValues() != null) {
				return DataConverter.getValue("timezone", organisation);
			}
		}
		return null;
	}
	
	public User getUser(long id) {
		return (User) getFromCache(getUserCacheKey(id),
				new UserByIdUpdateTask(id), () -> msService.getUser(id), 120000l);
	}
	
	public AdditionalServiceItem getAdditionalService(String id) {
		if (id == null
				|| id.trim().isEmpty()) {
			return null;
		}
		try {
			return getAdditionalService(Long.parseLong(id));
		} catch (Exception e) {
			LOGGER.error("Additional service id " + id + " is not number");
		}
		return null;
	}
	
	public AdditionalServiceItem getAdditionalService(long id) {
		return (AdditionalServiceItem) getFromCache(getAdditionalServiceCacheKey(id),
				new AdditionalServiceByIdUpdateTask(id), () -> msService.getAdditionalService(id), 120000l);
	}
	
	public Trip getTrip(String id) {
		if (id == null
				|| id.trim().isEmpty()) {
			return null;
		}
		try {
			return getTrip(Long.parseLong(id));
		} catch (Exception e) {
			LOGGER.error("Trip id " + id + " is not number");
		}
		return null;
	}
	
	public Trip getTrip(long id) {
		return (Trip) getFromCache(getTripCacheKey(id),
				new TripByIdUpdateTask(id), () -> msService.getTripWithParentsChilds(id), 120000l);
	}
	
	public Trip getTripWithoutCache(long id) {
		return msService.getTripWithParentsChilds(id);
	}
	
	public Organisation getOrganisation(long id) {
		return getAllOrganisations().get(getEntityId(id));
	}
	
	public Organisation getMappedOrganisation(long mappingId) {
		return getAllOrganisations().get(getMappingId(mappingId));
	}
	
	public com.gillsoft.ms.entity.ResourceParams getResourceParam(long resourceParamsId) {
		return getAllResourceParams().get(getEntityId(resourceParamsId));
	}
	
	public Resource getResource(com.gillsoft.ms.entity.ResourceParams params) {
		if (params == null) {
			return null;
		}
		for (BaseEntity entity : params.getParents()) {
			if (entity.getType() == EntityType.RESOURCE) {
				return (Resource) entity;
			}
		}
		return null;
	}
	
	public Object getFromCache(String cacheKey, Runnable updateTask, CacheObjectGetter objectGetter, long updateDelay) {
		
		// берем результат с кэша, если кэша нет, то берем напрямую с сервиса
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, cacheKey);
		try {
			Object object = cache.read(params);
			if (object == null) {
				
				// синхронизация по ключу кэша
				synchronized (cacheKey.intern()) {
					object = cache.read(params);
					if (object == null) {
						object = objectGetter.forCache();
						params.put(MemoryCacheHandler.IGNORE_AGE, true);
						params.put(MemoryCacheHandler.UPDATE_DELAY, updateDelay);
						params.put(MemoryCacheHandler.UPDATE_TASK, updateTask);
						try {
							cache.write(object, params);
						} catch (IOCacheException writeException) {
						}
					}
					return object;
				}
			} else {
				return object;
			}
		} catch (IOCacheException e) {
			LOGGER.error("Error in cache", e);
			return null;
		}
	}
	
	public Price recalculate(Segment segment, Price price, Currency currency) {
		if (price.getCommissions() != null) {
			price.getCommissions().forEach(c -> c.setId(null));
		}
		addCommissions(getCommissions(segment), price);
		Price calculated = calculator.calculateResource(price, getUser(), currency, getTariffMarkups(segment));
		calculated.setSource(calculator.copy(price));
		addReturnConditions(segment, calculated);
		return calculated;
	}
	
	private void addCommissions(List<com.gillsoft.model.Commission> commissions, Price price) {
		if (commissions != null) {
			if (price.getCommissions() == null) {
				price.setCommissions(commissions);
			} else {
				price.getCommissions().addAll(commissions);
			}
			for (com.gillsoft.model.Commission commission : price.getCommissions()) {
				if (commission.getCurrency() == null) {
					commission.setCurrency(price.getCurrency());
				}
				if (commission.getVat() == null) {
					commission.setVat(BigDecimal.ZERO);
					commission.setVatCalcType(CalcType.IN);
				}
			}
		}
	}
	
	public Price recalculate(com.gillsoft.model.AdditionalServiceItem additionalService, Price price, Currency currency) {
		if (price.getCommissions() != null) {
			price.getCommissions().forEach(c -> c.setId(null));
		}
		List<com.gillsoft.model.Commission> commissions = getCommissions();
		if (commissions != null) {
			if (price.getCommissions() == null) {
				price.setCommissions(commissions);
			} else {
				price.getCommissions().addAll(commissions);
			}
			for (com.gillsoft.model.Commission commission : price.getCommissions()) {
				if (commission.getCurrency() == null) {
					commission.setCurrency(price.getCurrency());
				}
				if (commission.getVat() == null) {
					commission.setVat(BigDecimal.ZERO);
					commission.setVatCalcType(CalcType.IN);
				}
			}
		}
		Price calculated = calculator.calculateResource(price, getUser(), currency);
		calculated.setSource(calculator.copy(price));
		addReturnConditions(additionalService, calculated);
		return calculated;
	}
	
	public List<com.gillsoft.model.Commission> getCommissions() {
		List<BaseEntity> entities = getParentEntities(null);
		if (entities != null) {
			return getCommissions(entities);
		}
		return null;
	}
	
	public List<com.gillsoft.model.Commission> getCommissions(Segment segment) {
		List<BaseEntity> entities = getParentEntities(segment);
		if (entities != null) {
			return getCommissions(entities);
		}
		return null;
	}
	
	public List<TariffMarkup> getTariffMarkups(Segment segment) {
		List<BaseEntity> entities = getParentEntities(segment);
		if (entities != null) {
			return getTariffMarkups(entities);
		}
		return null;
	}
	
	public List<AdditionalServiceItem> getAdditionalServices(Segment segment) {
		List<BaseEntity> entities = getParentEntities(segment);
		if (entities != null) {
			List<AdditionalServiceItem> services = getAdditionalServices(entities);
			if (services != null) {
				return services.stream().filter(AdditionalServiceItem::isUsedWithTrip).collect(Collectors.toList());
			}
		}
		return null;
	}
	
	public Price recalculateReturn(Segment segment, String timeZone, Price price, Price resourcePrice) {
		
		// сборы без ид - сборы от ресурса
		// сборы с ид - начисленные системой GDS
		if (resourcePrice != null
				&& resourcePrice.getCommissions() != null) {
			resourcePrice.getCommissions().forEach(c -> c.setId(null));
		}
		addReturnConditions(segment, price);
		price.setReturned(calculator.calculateReturn(price, resourcePrice, getUser(), price.getCurrency(),
				new Date(Utils.getCurrentTimeInMilis(timeZone)), segment.getDepartureDate()));
		
		// устанавливаем исходную сумму возврата от ресурса
		price.getReturned().setSource(calculator.copy(resourcePrice));
		return price;
	}
	
	private void addReturnConditions(Segment segment, Price price) {
		// условия возврата для стоимости установленные на организацию
		List<com.gillsoft.model.ReturnCondition> conditions = getReturnConditions(segment);
		addReturnConditions(conditions, price);
	}
	
	private void addReturnConditions(List<com.gillsoft.model.ReturnCondition> conditions, Price price) {
		if (conditions != null) {
			if (price.getTariff().getReturnConditions() == null) {
				price.getTariff().setReturnConditions(conditions);
			} else {
				price.getTariff().getReturnConditions().forEach(c -> c.setId(null));
				price.getTariff().getReturnConditions().addAll(conditions);
			}
		}
		if (price.getTariff().getReturnConditions() != null) {
			price.getTariff().getReturnConditions().removeIf(r -> r.getReturnPercent() == null);
		}
		// условия возврата каждого сбора
		if (price.getCommissions() != null) {
			for (com.gillsoft.model.Commission commission : price.getCommissions()) {
				if (commission.getId() != null) {
					BaseEntity entity = new BaseEntity();
					try {
						entity.setId(Long.parseLong(commission.getId()));
						List<com.gillsoft.model.ReturnCondition> commissionConditions = getReturnConditions(Collections.singletonList(entity));
						if (commissionConditions != null) {
							commission.setReturnConditions(commissionConditions);
						}
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}
	
	public void addReturnConditions(com.gillsoft.model.AdditionalServiceItem serviceItem, Price price) {
		// условия возврата для стоимости установленные на организацию
		List<com.gillsoft.model.ReturnCondition> conditions = null;
		BaseEntity entity = new BaseEntity();
		try {
			entity.setId(Long.parseLong(serviceItem.getId()));
			conditions = getReturnConditions(Collections.singletonList(entity));
		} catch (NumberFormatException e) {
		}
		addReturnConditions(conditions, price);
	}
	
	public List<com.gillsoft.model.ReturnCondition> getReturnConditions(Segment segment) {
		List<BaseEntity> entities = getParentEntities(segment);
		if (entities != null) {
			return getReturnConditions(entities);
		}
		return null;
	}
	
	public List<TicketLayout> getTicketLayouts() {
		List<BaseEntity> entities = getParentEntities(null);
		if (entities != null) {
			return getTicketLayouts(entities);
		}
		return null;
	}
	
	public List<ServiceFilter> getFilters(Segment segment) {
		List<BaseEntity> entities = getParentEntities(segment);
		if (entities != null) {
			return getFilters(entities);
		}
		return null;
	}
	
	public List<ResourceFilter> getResourceFilters() {
		List<BaseEntity> entities = getParentEntities(null);
		if (entities != null) {
			return getResourceFilters(entities);
		}
		return null;
	}
	
	public List<ResourceConnection> getResourceConnections() {
		List<BaseEntity> entities = getParentEntities(null);
		if (entities != null) {
			return getResourceConnections(entities);
		}
		return null;
	}
	
	public List<ConnectionDiscount> getResourceConnectionDiscounts() {
		List<BaseEntity> entities = getParentEntities(null);
		if (entities != null) {
			return getResourceConnectionDiscounts(entities);
		}
		return null;
	}
	
	public List<OrderAccess> getOrdersAccess(User user) {
		List<BaseEntity> entities = getParentEntities(user, null);
		if (entities != null) {
			return getOrdersAccess(entities);
		}
		return null;
	}
	
	private List<BaseEntity> getParentEntities(Segment segment) {
		return getParentEntities(getUser(), segment);
	}
	
	private List<BaseEntity> getParentEntities(User user, Segment segment) {
		if (user != null) {
			List<BaseEntity> entities = new ArrayList<>();
			entities.add(user);
			BaseEntity parent = user;
			while ((parent = (parent.getParents() != null && parent.getParents().iterator().hasNext()
					? parent.getParents().iterator().next() : null)) != null) {
				entities.add(parent);
			}
			if (segment != null) {
				
				// добавляем сущность ресурса
				Resource resource = getResource(segment.getResource().getId());
				if (resource != null) {
					entities.add(resource);
				}
				addMappedOrganisations(entities, segment);
				// TODO add segment object's ids which mapping in system
			}
			// TODO add ids of price tariff and commissions
			return entities;
		}
		return null;
	}
	
	private void addMappedOrganisations(List<BaseEntity> entities, Segment segment) {
		addMappedOrganisation(entities, segment.getCarrier());
		addMappedOrganisation(entities, segment.getInsurance());
	}
	
	private void addMappedOrganisation(List<BaseEntity> entities, com.gillsoft.model.Organisation organisation) {
		if (organisation != null) {
			try {
				long orgId = Long.parseLong(organisation.getId());
				Organisation org = getOrganisation(orgId);
				if (org != null) {
					entities.add(org);
				}
			} catch (NumberFormatException e) {
			}
		}
	}
	
	public List<com.gillsoft.model.Commission> getCommissions(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllCommissions());
		if (codeEntities != null) {
			
			// конвертируем из комиссий базы в комиссии апи gds-commons
			return codeEntities.stream().map(c -> DataConverter.convert((Commission) c)).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<TariffMarkup> getTariffMarkups(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllTariffMarkups());
		if (codeEntities != null) {
			return codeEntities.stream().map(tm -> (TariffMarkup) tm).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<AdditionalServiceItem> getAdditionalServices(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllAdditionalServices());
		if (codeEntities != null) {
			return codeEntities.stream().map(as -> (AdditionalServiceItem) as).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<com.gillsoft.model.ReturnCondition> getReturnConditions(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllReturnConditions());
		if (codeEntities != null) {
			
			// конвертируем из условий возврата базы в условия возврата апи gds-commons
			return codeEntities.stream().map(c -> DataConverter.convert((ReturnCondition) c)).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<TicketLayout> getTicketLayouts(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllTicketLayouts());
		if (codeEntities != null) {
			return codeEntities.stream().map(e -> (TicketLayout) e).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<ServiceFilter> getFilters(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllFilters());
		if (codeEntities != null) {
			return codeEntities.stream().map(e -> (ServiceFilter) e).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<ResourceFilter> getResourceFilters(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllResourceFilters());
		if (codeEntities != null) {
			return codeEntities.stream().map(e -> (ResourceFilter) e).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<ResourceConnection> getResourceConnections(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllResourceConnections());
		if (codeEntities != null) {
			return codeEntities.stream().map(e -> (ResourceConnection) e).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<ConnectionDiscount> getResourceConnectionDiscounts(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllResourceConnectionDiscounts());
		if (codeEntities != null) {
			return codeEntities.stream().map(e -> (ConnectionDiscount) e).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<OrderAccess> getOrdersAccess(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllOrdersAccess());
		if (codeEntities != null) {
			return codeEntities.stream().map(e -> (OrderAccess) e).collect(Collectors.toList());
		}
		return null;
	}
	
	public Collection<CodeEntity> getCodeEntities(List<BaseEntity> entities, Map<Long, List<CodeEntity>> childs) {
		if (childs != null) {
			
			// нужны только уникальные сущности
			Map<Long, CodeEntity> mappedChilds = new HashMap<>();
			for (BaseEntity entity : entities) {
				List<CodeEntity> entityChilds = childs.get(entity.getId());
				if (entityChilds != null) {
					long currTime = System.currentTimeMillis();
					
					// отбираем только действующие сущности
					mappedChilds.putAll(entityChilds.stream().filter(c -> 
							(c.getStart() == null || c.getStart().getTime() <= currTime)
							&& (c.getEnd() == null || c.getEnd().getTime() >= currTime))
							.collect(Collectors.toMap(BaseEntity::getId, c -> c, (c1, c2) -> c1)));
				}
			}
			if (!mappedChilds.isEmpty()) {
				
				// выбираем сущности, у которых все паренты есть в переданном списке
				Map<String, CodeEntity> result = new HashMap<>();
				Set<Long> entityIds = entities.stream().map(BaseEntity::getId).collect(Collectors.toSet());
				for (CodeEntity child : mappedChilds.values()) {
					if (child.getParents() != null) {
						Set<Long> parentIds = child.getParents().stream().map(BaseEntity::getId).collect(Collectors.toSet());
						if (entityIds.containsAll(parentIds)) {
							
							// берем сущности с одинаковым кодом и оставляем только те, у которых больше веса всех родителей
							BaseEntity compared = result.get(child.getCode());
							if (compared == null
									|| getWeight(child) > getWeight(compared)) {
								result.put(child.getCode(), child);
							}
						}
					}
				}
				return result.values();
			}
		}
		return null;
	}
	
	private int getWeight(BaseEntity entity) {
		return entity.getParents().stream().mapToInt(e -> e.getType().getWeight()).sum();
	}
	
	/**
	 * Возвращает список макетов билетов по ресурсам заказа.
	 * 
	 * @param order
	 *            Заказ.
	 * @return Список макетов билетов.
	 */
	@SuppressWarnings("unchecked")
	public Map<Long, List<TicketLayout>> getTicketLayouts(Order order) {
		Map<Long, List<CodeEntity>> all = getAllTicketLayouts();
		
		// получаем макеты билетов для каждого ресурса заказа
		long currTime = System.currentTimeMillis();
		Map<Long, List<TicketLayout>> layouts = order.getOrders().stream().collect(
				Collectors.toMap(
						ResourceOrder::getResourceId,
						r -> {
							if (all != null
									&& all.containsKey(r.getResourceId())) {
								
								// проверяем период действия
								return (List<TicketLayout>)(List<?>) all.get(r.getResourceId()).stream().filter(c ->
											(c.getStart() == null || c.getStart().getTime() <= currTime)
											&& (c.getEnd() == null || c.getEnd().getTime() >= currTime))
										.collect(Collectors.toList());
							}
							return Collections.EMPTY_LIST;
						},
						(r1, r2) -> r1));
		// получаем макеты переопределенные для конкретного пользователя
		List<TicketLayout> userLayouts = getTicketLayouts();
		
		// меняем макеты по ресурсам на переопределенные для пользователя
		if (userLayouts != null
				&& !userLayouts.isEmpty()) {
			for (TicketLayout ticketLayout : userLayouts) {
				for (Entry<Long, List<TicketLayout>> entry : layouts.entrySet()) {
					if (entry.getValue().isEmpty()) {
						Optional<TicketLayout> optional = entry.getValue().stream().filter(l -> Objects.equals(l.getCode(), ticketLayout.getCode())).findFirst();
						if (optional.isPresent()) {
							entry.getValue().remove(optional.get());
							entry.getValue().add(ticketLayout);
						}
					} else {
						entry.setValue(Arrays.asList(ticketLayout));
					}
				}
			}
		}
		return layouts;
	}
	
	public boolean isOrderAvailable(Order order, ServiceStatus newStatus) {
		
		// список пользователей заказа
		Set<Long> users = getOrderUsers(order);
		
		// текущий пользователь
		User curr = getUser();
		
		// список сущностей иерархии пользователя
		List<BaseEntity> userEntities = getParentEntities(curr, null);
		
		// список доступов к заказу текущего пользователя
		List<OrderAccess> access = getAvalaibleOrdersAccess(curr, userEntities);
		for (Long id : users) {
			
			// для сервисов принадлежащих текущему пользователю
			if (curr.getId() == id) {
				
				// если доступов нет, то все разрешено
				if (access != null
						&& !access.isEmpty()) {
					setUnavalaibleServiceStatus(order, newStatus, access, id);
				}
			// для сервисов других пользователей
			} else {
				// получаем статусы, у которых есть дети из списка userEntities
				List<OrderAccess> currUserAccess = getAvalaibleOrdersAccess(getUser(id), userEntities);
				setUnavalaibleServiceStatus(order, newStatus, currUserAccess, id);
			}
		}
		return isPresentAvalaibleStatus(order);
	}
	
	private Set<Long> getOrderUsers(Order order) {
		Set<Long> users = new HashSet<>();
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				for (ServiceStatusEntity statusEntity : resourceService.getStatuses()) {
					users.add(statusEntity.getUserId());
				}
			}
		}
		return users;
	}
	
	private List<OrderAccess> getAvalaibleOrdersAccess(User user, List<BaseEntity> userEntities) {
		List<OrderAccess> userAccess = new ArrayList<>();
		
		// все статусы доступные указанному пользователю (пользователь или его родители есть парентами для статуса доступа)
		List<OrderAccess> access = getOrdersAccess(user);
		if (access != null) {
			for (OrderAccess orderAccess : access) {
				
				// если дети доступного статуса содержат хоть какую-нибудь сущность из указанных в userEntities
				if (orderAccess.getChilds() != null
						&& orderAccess.getChilds().stream().anyMatch(c -> userEntities.stream().anyMatch(e -> c.getId() == e.getId()))) {
					userAccess.add(orderAccess);
				}
			}
		}
		return userAccess;
	}
	
	private void setUnavalaibleServiceStatus(Order order, ServiceStatus newStatus, List<OrderAccess> access, long userId) {
		
		// если доступы есть, то проверяем статус сервисов
		// пользователя и статус, в который хотим перевести
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				for (ServiceStatusEntity statusEntity : resourceService.getStatuses()) {
					if (statusEntity.getUserId() == userId) {
						if (!isPresentStatus(access, newStatus)) {
							
							// проставляем сервису статус UNAVAILABLE, чтобы пользователь не мог его обрабатывать
							statusEntity.setPrevStatus(statusEntity.getStatus());
							statusEntity.setStatus(ServiceStatus.UNAVAILABLE);
						}
					}
				}
			}
		}
	}
	
	private boolean isPresentStatus(List<OrderAccess> access, ServiceStatus status) {
		return access.stream().anyMatch(a -> a.getAvailableStatus() == status);
	}
	
	private boolean isPresentAvalaibleStatus(Order order) {
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				for (ServiceStatusEntity statusEntity : resourceService.getStatuses()) {
					if (statusEntity.getStatus() != ServiceStatus.UNAVAILABLE) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public CacheHandler getCache() {
		return cache;
	}

	public static String getActiveResourcesCacheKey(String userName) {
		return ACTIVE_RESOURCES_CACHE_KEY + userName;
	}

	public static String getAllCommissionsKey() {
		return ALL_COMMISSIONS_KEY;
	}
	
	public static String getAllTariffMarkupsKey() {
		return ALL_TARIFF_MARKUPS_KEY;
	}
	
	public static String getAllAdditionalServicesKey() {
		return ALL_ADDITIONAL_SERVICES_KEY;
	}
	
	public static String getAllReturnConditionsKey() {
		return ALL_RETURN_CONDITIONS_KEY;
	}
	
	public static String getAllTicketLayoutsKey() {
		return ALL_TICKET_LAYOUTS_KEY;
	}
	
	public static String getAllFiltersKey() {
		return ALL_FILTERS_KEY;
	}
	
	public static String getAllOrdersAccessKey() {
		return ALL_ORDERS_ACCESS_KEY;
	}
	
	public static String getAllResourceFiltersKey() {
		return ALL_RESOURCE_FILTERS_KEY;
	}
	
	public static String getAllResourceConnectionsKey() {
		return ALL_RESOURCE_CONNECTIONS_KEY;
	}
	
	public static String getAllResourceConnectionDiscountsKey() {
		return ALL_RESOURCE_CONNECTION_DISCOUNTS_KEY;
	}
	
	public static String getAllOrganisationsCacheKey() {
		return ALL_ORGANISATIONS_KEY;
	}
	
	public static String getAllResourceParamsCacheKey() {
		return ALL_RESOURCE_ORDERS_PARAMS_KEY;
	}
	
	public static String getUserCacheKey(String userName) {
		return USER_KEY + userName;
	}
	
	public static String getUserOrganisationCacheKey(String userName) {
		return USER_ORGANISATION_KEY + userName;
	}
	
	public static String getUserCacheKey(long id) {
		return USER_KEY + id;
	}
	
	public static String getTripCacheKey(long id) {
		return TRIP_KEY + id;
	}
	
	public static String getAdditionalServiceCacheKey(long id) {
		return ADDITIONAL_SERVICE_KEY + id;
	}
	
	public interface CacheObjectGetter {
		
		public Object forCache();
		
	}

}
