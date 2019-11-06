package com.gillsoft.control.core;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gillsoft.control.service.model.SearchRequestContainer;
import com.gillsoft.model.GdsDate;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.CodeEntity;
import com.gillsoft.ms.entity.EntityType;
import com.gillsoft.ms.entity.FilterType;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.ms.entity.ResourceFilter;
import com.gillsoft.ms.entity.ResourceFilterCondition;
import com.gillsoft.ms.entity.ServiceFilter;

@Component(value = "ResourceFilterController")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ResourceFilterController extends FilterController {
	
	private static Logger LOGGER = LogManager.getLogger(ResourceFilterController.class);
	
	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@PostConstruct
	@Scheduled(initialDelay = 600000, fixedDelay = 600000)
	public void updateFields() {
		Map<String, Field> fieldsMap = new HashMap<>();
		
		Map<Long, List<CodeEntity>> entities = dataController.getAllResourceFilters();
		
		Map<Long, List<CodeEntity>> activateFilters = new HashMap<>();
		Map<Long, List<BaseEntity>> filterConditions = new HashMap<>();
		entities.forEach((key, list) ->
			{
				activateFilters.put(key, list.stream().map(CodeEntity::getChilds).flatMap(Set::stream)
						.filter(v -> v.getType() == EntityType.FILTER).map(v -> (CodeEntity) v).collect(Collectors.toList()));
				filterConditions.put(key, list.stream().map(CodeEntity::getChilds).flatMap(Set::stream)
						.filter(v -> v.getType() == EntityType.RESOURCE_FILTER_CONDITION).collect(Collectors.toList()));
			}
		);
		updateServiceFilterFields(fieldsMap, activateFilters);
		updateFilterConditionFields(fieldsMap, filterConditions);
		this.fields = fieldsMap;
	}
	
	protected void updateFilterConditionFields(Map<String, Field> fieldsMap, Map<Long, List<BaseEntity>> entities) {
		if (entities != null) {
			for (List<BaseEntity> filters : entities.values()) {
				for (BaseEntity entity : filters) {
					ResourceFilterCondition filter = (ResourceFilterCondition) entity;
					String[] name = filter.getFilteredField().split("\\.");
					addFilteredField(fieldsMap, name);
				}
			}
		}
	}
	
	public void apply(SearchRequestContainer requestContainer) {
		List<ResourceFilter> filters = dataController.getResourceFilters();
		if (filters == null
				|| filters.isEmpty()) {
			for (List<TripSearchRequest> requests : requestContainer.getPairRequests().values()) {
				requests.forEach(r -> r.setPermittedToResult(true));
			}
		} else {
			Set<String> disabledRequests = getDisabledSubRequests(requestContainer, filters);
			
			for (ResourceFilter resourceFilter : filters) {
				
				// ресурсы от которых зависит фильтр (главные - их надо ждать)
				List<Resource> mainResources = resourceFilter.getChilds().stream()
						.filter(child -> child.getType() == EntityType.RESOURCE).map(child -> (Resource) child).collect(Collectors.toList());
				if (!mainResources.isEmpty()) {
					
					// фильтры главных рейсов
					List<ServiceFilter> serviceFilters = resourceFilter.getChilds().stream()
							.filter(child -> child.getType() == EntityType.FILTER).map(child -> (ServiceFilter) child).collect(Collectors.toList());
					
					// условия фильтрации зависимых рейсов
					List<ResourceFilterCondition> filterConditions = resourceFilter.getChilds().stream()
							.filter(child -> child.getType() == EntityType.RESOURCE_FILTER_CONDITION).map(child -> (ResourceFilterCondition) child).collect(Collectors.toList());
					
					// зависимый ресурс (его надо фильтровать)
					Resource subResource = resourceFilter.getResource();
					for (List<TripSearchRequest> requests : requestContainer.getPairRequests().values()) {
						out: {
							
							// ищем есть ли зависимые запросы поиска
							List<TripSearchRequest> subRequests = requests.stream()
									.filter(r -> String.valueOf(subResource.getId()).equals(r.getParams().getResource().getId())).collect(Collectors.toList());
							if (!subRequests.isEmpty()) {
								
								// проверяем есть ли главные ресурсы в запросах
								List<TripSearchRequest> mainRequests = new ArrayList<>();
								for (TripSearchRequest request : requests) {
									if (mainResources.stream().filter(r -> String.valueOf(r.getId()).equals(request.getParams().getResource().getId()))
											.findFirst().isPresent()) {
										mainRequests.add(request);
										
										// если поиск на главном ресурсе не окончен, то зависимые тоже не завершаем
										if (!request.isSearchCompleted()) {
											break out;
										}
									}
								}
								// проверяем рейсы зависимого ресурса с главными
								if (!mainRequests.isEmpty()) {
									for (TripSearchRequest subRequest : subRequests) {
										List<TripContainer> subContainers = requestContainer.getResponse().getTripContainers().stream()
												.filter(c -> Objects.equals(c.getRequest().getId(), subRequest.getId())).collect(Collectors.toList());
										if (!subContainers.isEmpty()) {
											for (TripContainer subContainer : subContainers) {
												if (subContainer.getTrips() != null
														&& !subContainer.getTrips().isEmpty()) {
													for (Trip subTrip : subContainer.getTrips()) {
														Segment subSegment = requestContainer.getResponse().getSegments().get(subTrip.getId());
														if (subSegment != null
																&& !subSegment.isChecked()) {
															
															// проверяем главные рейсы на прохождение фильтра
															for (TripSearchRequest mainRequest : mainRequests) {
																List<TripContainer> mainContainers = requestContainer.getResponse().getTripContainers().stream()
																		.filter(c -> Objects.equals(c.getRequest().getId(), mainRequest.getId())).collect(Collectors.toList());
																if (!mainContainers.isEmpty()) {
																	for (TripContainer mainContainer : mainContainers) {
																		if (mainContainer.getTrips() != null
																				&& !mainContainer.getTrips().isEmpty()) {
																			for (Trip mainTrip : mainContainer.getTrips()) {
																				Segment mainSegment = requestContainer.getResponse().getSegments().get(mainTrip.getId());
																				if (mainSegment != null) {
																					
																					// если фильтров нет, то сравниваем зависимые рейсы со всеми главными
																					// иначе только с теми, которые прошли фильтры
																					if (!serviceFilters.isEmpty()) {
																						
																						// если remove, то не проверяем зависимые рейсы
																						// иначе - проверяем
																						if (!isRemove(mainSegment, serviceFilters)
																								
																								// проверяем зависимый рейс на прохождение главного
																								&& isRemove(subSegment, mainSegment, filterConditions, resourceFilter.getFilterType())) {
																							requestContainer.getResponse().getSegments().remove(subTrip.getId());
																						}
																						// проверяем зависимый рейс на прохождение главного
																					} else if (isRemove(subSegment, mainSegment, filterConditions, resourceFilter.getFilterType())) {
																						requestContainer.getResponse().getSegments().remove(subTrip.getId());
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			List<TripSearchRequest> allRequests = requestContainer.getPairRequests().values().stream().flatMap(List::stream).collect(Collectors.toList());
			allRequests.forEach(r -> {
				if (!disabledRequests.contains(r.getId())) {
					r.setPermittedToResult(true);
				} else {
					r.setPermittedToResult(false);
				}
			});
		}
	}
	
	/*
	 * Проверяет можно ли показывать зависимые запросы или нет.
	 * Возвращает список заблокированных запросов ожидающих главных ресурсов.
	 */
	private Set<String> getDisabledSubRequests(SearchRequestContainer requestContainer, List<ResourceFilter> filters) {
		Set<String> disabledRequests = new HashSet<>(); // результат, который нельзя показывать
		for (ResourceFilter resourceFilter : filters) {
						
			// ресурсы от которых зависит фильтр (главные - их надо ждать)
			List<Resource> mainResources = resourceFilter.getChilds().stream()
					.filter(child -> child.getType() == EntityType.RESOURCE).map(child -> (Resource) child).collect(Collectors.toList());
			if (!mainResources.isEmpty()) {
				
				// зависимый ресурс (его надо фильтровать)
				Resource subResource = resourceFilter.getResource();
				for (List<TripSearchRequest> requests : requestContainer.getPairRequests().values()) {
						
					// ищем есть ли зависимые запросы поиска
					List<TripSearchRequest> subRequests = requests.stream()
							.filter(r -> String.valueOf(subResource.getId()).equals(r.getParams().getResource().getId())).collect(Collectors.toList());
					if (!subRequests.isEmpty()) {
						
						// проверяем есть ли главные ресурсы в запросах
						for (TripSearchRequest request : requests) {
							if (mainResources.stream().filter(r -> String.valueOf(r.getId()).equals(request.getParams().getResource().getId()))
									.findFirst().isPresent()) {
								
								// если поиск на главном ресурсе не окончен, то зависимые тоже не завершаем
								if (!request.isSearchCompleted()) {
									subRequests.forEach(r -> disabledRequests.add(r.getId()));
									break;
								}
							}
						}
					}
				}
			}
		}
		return disabledRequests;
	}
	
	private boolean isRemove(Segment subSegment, Segment mainSegment, List<ResourceFilterCondition> conditions, FilterType type) {
		subSegment.setChecked(true);
		if (conditions != null
				&& !conditions.isEmpty()) {
			
			// условия на одинаковые поля применяются через OR, на разные - AND
			Map<String, List<ResourceFilterCondition>> conditionsMap = conditions.stream().collect(
					Collectors.groupingBy(ResourceFilterCondition::getFilteredField));
			for (List<ResourceFilterCondition> groupe : conditionsMap.values()) {
				boolean remove = true;
				for (ResourceFilterCondition condition : groupe) {
					String[] name = condition.getFilteredField().split("\\.");
					Object subValue = getValue(subSegment, name);
					Object mainValue = getValue(mainSegment, name);
					if (subValue != null
							&& mainValue != null) {
						remove = remove
								&& isAccepted(modifyValue(subValue, condition).toLowerCase(),
										condition.getComparison(), toString(mainValue).toLowerCase());
					}
				}
				// если remove = false, то нет смысла проверять другие условия
				// так как они применяются через AND
				if (!remove) {
					return type == FilterType.INCLUDE;
				}
			}
			return type == FilterType.EXCLUDE;
		}
		return true;
	}
	
	private String toString(Object value) {
		if (value instanceof Date) {
			return new GdsDate(((Date) value).getTime()).toString();
		} else if (value instanceof Number) {
			try {
				return new BigDecimal(value.toString()).toString();
			} catch (NumberFormatException e) {
				return value.toString();
			}
		}
		return value.toString();
	}
	
	/*
	 * Меняет значение на величину условия. 
	 */
	private String modifyValue(Object value, ResourceFilterCondition condition) {
		if (condition.getValue() == null) {
			return value.toString();
		}
		if (value instanceof Date) {
			Date date = (Date) value;
			try {
				long modValue = Long.parseLong(condition.getValue());
				switch (condition.getOperation()) {
				case PLUS:
					return new GdsDate(date.getTime() + modValue).toString();
				case MINUS:
					return new GdsDate(date.getTime() - modValue).toString();
				default:
					return value.toString();
				}
			} catch (NumberFormatException e) {
				return value.toString();
			}
		} else if (value instanceof Number) {
			try {
				BigDecimal decimal = new BigDecimal(value.toString());
				BigDecimal modValue = new BigDecimal(condition.getValue().toString());
				switch (condition.getOperation()) {
				case PLUS:
					return decimal.add(modValue).toString();
				case MINUS:
					return decimal.subtract(modValue).toString();
				default:
					return value.toString();
				}
			} catch (NumberFormatException e) {
				return value.toString();
			}
		}
		return value.toString();
	}
	
	private Object getValue(Segment segment, String[] name) {
		Object value = segment;
		for (int i = 1; i < name.length; i++) {
			Field field = fields.get(getKey(name, i));
			if (field == null) {
				break;
			}
			try {
				value = field.get(value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				getLogger().error("Can not get value of field " + name[0], e);
				break;
			}
			if (value == null) {
				break;
			}
		}
		return value;
	}
	
}
