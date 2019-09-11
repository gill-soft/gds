package com.gillsoft.control.core;

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

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.commission.Calculator;
import com.gillsoft.control.service.MsDataService;
import com.gillsoft.control.service.model.Order;
import com.gillsoft.control.service.model.ResourceOrder;
import com.gillsoft.control.service.model.ResourceService;
import com.gillsoft.control.service.model.ServiceStatusEntity;
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Price;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceStatus;
import com.gillsoft.model.ValueType;
import com.gillsoft.model.request.ResourceParams;
import com.gillsoft.ms.entity.AttributeValue;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.CodeEntity;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.OrderAccess;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.ServiceFilter;
import com.gillsoft.ms.entity.TicketLayout;
import com.gillsoft.ms.entity.User;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class MsDataController {
	
	private static Logger LOGGER = LogManager.getLogger(MsDataController.class);
	
	private static final String ACTIVE_RESOURCES_CACHE_KEY = "active.resources.";
	
	private static final String ALL_COMMISSIONS_KEY = "all.commissions";
	
	private static final String ALL_RETURN_CONDITIONS_KEY = "all.return.conditions";
	
	private static final String ALL_TICKET_LAYOUTS_KEY = "all.ticket.layouts";
	
	private static final String ALL_FILTERS_KEY = "all.filters";
	
	private static final String ALL_ORDERS_ACCESS_KEY = "all.orders.access";
	
	private static final String USER_KEY = "user.";
	
	private static final String ORGANISATION_KEY = "organisation.";
	
	@Autowired
	private MsDataService msService;
	
	@Autowired
    @Qualifier("MemoryCacheHandler")
	private CacheHandler cache;
	
	@SuppressWarnings("unchecked")
	public List<Resource> getUserResources() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		String userName = authentication.getName();
		return (List<Resource>) getFromCache(getActiveResourcesCacheKey(userName),
				new UserResourcesUpdateTask(userName), () -> new CopyOnWriteArrayList<>(msService.getUserResources(userName)), 600000l);
	}
	
	public ResourceParams createResourceParams(long resourceId) {
		List<Resource> resources = getUserResources();
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
	@PostConstruct
	public Map<Long, List<CodeEntity>> getAllCommissions() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllCommissionsKey(),
				new AllCommissionsUpdateTask(), () -> toMap(msService.getAllCommissions()), 600000l);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public Map<Long, List<CodeEntity>> getAllReturnConditions() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllReturnConditionsKey(),
				new AllReturnConditionsUpdateTask(), () -> toMap(msService.getAllReturnConditions()), 600000l);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public Map<Long, List<CodeEntity>> getAllTicketLayouts() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllTicketLayoutsKey(),
				new AllTicketLayoutsUpdateTask(), () -> toMap(msService.getAllTicketLayouts()), 600000l);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public Map<Long, List<CodeEntity>> getAllFilters() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllFiltersKey(),
				new AllFiltersUpdateTask(), () -> toMap(msService.getAllFilters()), 600000l);
	}
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	public Map<Long, List<CodeEntity>> getAllOrdersAccess() {
		
		// используют все, по-этому создаем конкурирующую мапу с такими же значениями
		return (Map<Long, List<CodeEntity>>) getFromCache(getAllOrdersAccessKey(),
				new AllOrdersAccessUpdateTask(), () -> toMap(msService.getAllOrdersAccess()), 600000l);
	}
	
	public Map<Long, List<BaseEntity>> toMap(List<? extends BaseEntity> entities) {
		if (entities != null) {
			
			// entity id -> list of entities
			Map<Long, List<BaseEntity>> grouping = new ConcurrentHashMap<>();
			entities.forEach(entity -> {
				entity.setParents(new CopyOnWriteArraySet<>(entity.getParents()));
				for (BaseEntity parent : entity.getParents()) {
					List<BaseEntity> groupe = grouping.get(parent.getId());
					if (groupe == null) {
						groupe = new CopyOnWriteArrayList<>();
						grouping.put(parent.getId(), groupe);
					}
					groupe.add(entity);
				}
			});
			return grouping;
		} else {
			return null;
		}
	}
	
	public User getUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		String userName = authentication.getName();
		return (User) getFromCache(getUserCacheKey(userName),
				new UserByNameUpdateTask(userName), () -> msService.getUser(userName), 600000l);
	}
	
	public String getUserTimeZone() {
		User user = getUser();
		if (user.getAttributeValues() != null) {
			for (AttributeValue value : user.getAttributeValues()) {
				if (value.getAttribute().getName().equals("timezone")) {
					return value.getValue();
				}
			}
		}
		return null;
	}
	
	public User getUser(long id) {
		return (User) getFromCache(getUserCacheKey(id),
				new UserByIdUpdateTask(id), () -> msService.getUser(id), 600000l);
	}
	
	public Organisation getOrganisation(long id) {
		return (Organisation) getFromCache(getOrganisationCacheKey(id),
				new OrganisationUpdateTask(id), () -> msService.getOrganisation(id), 600000l);
	}
	
	private Object getFromCache(String cacheKey, Runnable updateTask, CacheObjectGetter objectGetter, long updateDelay) {
		
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
		List<com.gillsoft.model.Commission> commissions = getCommissions(segment);
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
		Price calculated = Calculator.calculateResource(price, getUser(), currency);
		calculated.setSource((Price) SerializationUtils.deserialize(SerializationUtils.serialize(price)));
		return calculated;
	}
	
	public Price recalculateReturn(Segment segment, String timeZone, Price price, Price resourcePrice) {
		if (resourcePrice != null
				&& resourcePrice.getCommissions() != null) {
			resourcePrice.getCommissions().forEach(c -> c.setId(null));
		}
		// условия возврата для стоимости установленные на организацию
		List<com.gillsoft.model.ReturnCondition> conditions = getReturnConditions(segment);
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
		price.setReturned(Calculator.calculateReturn(price, resourcePrice, getUser(), price.getCurrency(),
				new Date(Utils.getCurrentTimeInMilis(timeZone)), segment.getDepartureDate()));
		
		// устанавливаем исходную сумму возврата от ресурса
		price.getReturned().setSource((Price) SerializationUtils.deserialize(SerializationUtils.serialize(resourcePrice)));
		return price;
	}
	
	public List<com.gillsoft.model.Commission> getCommissions(Segment segment) {
		List<BaseEntity> entities = getParentEntities(segment);
		if (entities != null) {
			return getCommissions(entities);
		}
		return null;
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
	
	public List<ServiceFilter> getFilters() {
		List<BaseEntity> entities = getParentEntities(null);
		if (entities != null) {
			return getFilters(entities);
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
				List<Resource> resources = getUserResources();
				Optional<Resource> resource = resources.stream().filter(r -> String.valueOf(r.getId()).equals(segment.getResource().getId())).findFirst();
				if (resource.isPresent()) {
					entities.add(resource.get());
				}
				// TODO add segment object's ids which mapping in system
			}
			// TODO add ids of price tariff and commissions
			return entities;
		}
		return null;
	}
	
	public List<com.gillsoft.model.Commission> getCommissions(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllCommissions());
		if (codeEntities != null) {
			
			// конвертируем из комиссий базы в комиссии апи gds-commons
			return codeEntities.stream().map(c -> convert((Commission) c)).collect(Collectors.toList());
		}
		return null;
	}
	
	public List<com.gillsoft.model.ReturnCondition> getReturnConditions(List<BaseEntity> entities) {
		Collection<CodeEntity> codeEntities = getCodeEntities(entities, getAllReturnConditions());
		if (codeEntities != null) {
			
			// конвертируем из условий возврата базы в условия возврата апи gds-commons
			return codeEntities.stream().map(c -> convert((ReturnCondition) c)).collect(Collectors.toList());
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
				return result.values();
			}
		}
		return null;
	}
	
	private com.gillsoft.model.Commission convert(Commission commission) {
		com.gillsoft.model.Commission converted = new com.gillsoft.model.Commission();
		converted.setId(String.valueOf(commission.getId()));
		converted.setCode(commission.getCode());
		converted.setValue(commission.getValue());
		converted.setValueCalcType(CalcType.valueOf(commission.getValueCalcType().name()));
		converted.setVat(commission.getVat());
		converted.setVatCalcType(CalcType.valueOf(commission.getVatCalcType().name()));
		converted.setType(ValueType.valueOf(commission.getValueType().name()));
		converted.setCurrency(Currency.valueOf(commission.getCurrency().name()));
		converted.setName(getValue("name", commission));
		for (Lang lang : Lang.values()) {
			converted.setName(lang, getValue("name_" + lang.name(), commission));
		}
		return converted;
	}
	
	private com.gillsoft.model.ReturnCondition convert(ReturnCondition returnCondition) {
		com.gillsoft.model.ReturnCondition converted = new com.gillsoft.model.ReturnCondition();
		converted.setId(String.valueOf(returnCondition.getId()));
		converted.setMinutesBeforeDepart(returnCondition.getActiveTime());
		converted.setReturnPercent(returnCondition.getValue());
		converted.setTitle(getValue("name", returnCondition));
		for (Lang lang : Lang.values()) {
			converted.setTitle(lang, getValue("name_" + lang.name(), returnCondition));
		}
		return converted;
	}
	
	public com.gillsoft.model.User convert(User user) {
		com.gillsoft.model.User converted = new com.gillsoft.model.User();
		converted.setId(String.valueOf(user.getId()));
		converted.setName(getValue("name", user));
		converted.setEmail(getValue("email", user));
		converted.setPhone(getValue("phone", user));
		converted.setSurname(getValue("surname", user));
		converted.setPatronymic(getValue("patronymic", user));
		return converted;
	}
	
	public com.gillsoft.model.Organisation convert(Organisation organisation) {
		com.gillsoft.model.Organisation converted = new com.gillsoft.model.Organisation();
		converted.setAddress(getValue("address", organisation));
		for (Lang lang : Lang.values()) {
			converted.setAddress(lang, getValue("address_" + lang.name(), organisation));
		}
		converted.setName(getValue("name", organisation));
		for (Lang lang : Lang.values()) {
			converted.setName(lang, getValue("name_" + lang.name(), organisation));
		}
		
		String emails = getValue("email", organisation);
		if (emails != null) {
			converted.setEmails(Collections.singletonList(emails));
		}
		String phones = getValue("phone", organisation);
		if (phones != null) {
			converted.setPhones(Collections.singletonList(phones));
		}
		converted.setTradeMark(getValue("trade_mark", organisation));
		converted.setId(String.valueOf(organisation.getId()));
		return converted;
	}
	
	private String getValue(String name, BaseEntity entity) {
		AttributeValue value = getAttributeValue(name, entity);
		if (value != null) {
			return value.getValue();
		}
		return null;
	}
	
	private AttributeValue getAttributeValue(String name, BaseEntity entity) {
		for (AttributeValue value : entity.getAttributeValues()) {
			if (value.getAttribute() != null
					&& name.equals(value.getAttribute().getName())) {
				return value;
			}
		}
		return null;
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
		Map<Long, List<TicketLayout>> layouts = order.getOrders().stream().collect(
				Collectors.toMap(
						ResourceOrder::getResourceId,
						r -> {
							if (all != null
									&& all.containsKey(r.getResourceId())) {
								return (List<TicketLayout>)(List<?>) all.get(r.getResourceId());
							}
							return null;
						},
						(r1, r2) -> r1));
		// получаем макеты переопределенные для конкретного пользователя
		List<TicketLayout> userLayouts = getTicketLayouts();
		
		// меняем макеты по ресурсам на переопределенные для пользователя
		if (userLayouts != null
				&& !userLayouts.isEmpty()) {
			for (TicketLayout ticketLayout : userLayouts) {
				for (Entry<Long, List<TicketLayout>> entry : layouts.entrySet()) {
					if (entry.getValue() != null) {
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
		Set<Long> users = new HashSet<>();
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				for (ServiceStatusEntity statusEntity : resourceService.getStatuses()) {
					users.add(statusEntity.getUserId());
				}
			}
		}
		// результат
		boolean available = false;
		
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
				if (access == null
						|| access.isEmpty()) {
					available = true;
				} else {
					available = isServiceStatusAvalaible(order, newStatus, access, id);
				}
			// для сервисов других пользователей
			} else {
				// получаем статусы, у которых есть дети из списка userEntities
				List<OrderAccess> currUserAccess = getAvalaibleOrdersAccess(getUser(id), userEntities);
				available = isServiceStatusAvalaible(order, newStatus, currUserAccess, id);
			}
		}
		return available;
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
	
	private boolean isServiceStatusAvalaible(Order order, ServiceStatus newStatus, List<OrderAccess> access, long userId) {
		boolean available = false;
		
		// если доступы есть, то проверяем статус сервисов
		// пользователя и статус, в который хотим перевести
		for (ResourceOrder resourceOrder : order.getOrders()) {
			for (ResourceService resourceService : resourceOrder.getServices()) {
				for (ServiceStatusEntity statusEntity : resourceService.getStatuses()) {
					if (statusEntity.getUserId() == userId) {
						if (isPresentStatus(access, newStatus)) {
							available = true;
						} else {
							
							// проставляем сервису статус UNAVAILABLE, чтобы пользователь не мог его обрабатывать
							statusEntity.setPrevStatus(statusEntity.getStatus());
							statusEntity.setStatus(ServiceStatus.UNAVAILABLE);
						}
					}
				}
			}
		}
		return available;
	}
	
	private boolean isPresentStatus(List<OrderAccess> access, ServiceStatus status) {
		return access.stream().anyMatch(a -> a.getAvailableStatus() == status);
	}
	
	private int getWeight(BaseEntity entity) {
		return entity.getParents().stream().mapToInt(e -> e.getType().getWeight()).sum();
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
	
	public static String getUserCacheKey(String userName) {
		return USER_KEY + userName;
	}
	
	public static String getUserCacheKey(long id) {
		return USER_KEY + id;
	}
	
	public static String getOrganisationCacheKey(long id) {
		return ORGANISATION_KEY + id;
	}
	
	private interface CacheObjectGetter {
		
		public Object forCache();
		
	}

}
