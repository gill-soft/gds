package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.model.Unmapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.mapper.service.UnmappingConverter;
import com.gillsoft.model.Address;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Name;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Resource;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.RoutePoint;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.LangRequest;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TripSearchMapping {
	
	private static Logger LOGGER = LogManager.getLogger(TripSearchMapping.class);
	
	@Autowired
	private MappingService mappingService;
	
	@Autowired
	private MsDataController dataController;
	
	/**
	 * Создает пустые словари в ответе.
	 */
	public void createDictionaries(TripSearchResponse result) {
		
		// resource locality id -> locality from mapping
		if (result.getLocalities() == null) {
			result.setLocalities(new HashMap<>());
		}
		// resource resource id -> resource
		if (result.getResources() == null) {
			result.setResources(new HashMap<>());
		}
		// resource organisation id -> organisation from mapping
		if (result.getOrganisations() == null) {
			result.setOrganisations(new HashMap<>());
		}
		// resource vehicle id -> vehicle from mapping
		if (result.getVehicles() == null) {
			result.setVehicles(new HashMap<>());
		}
		// resourceId + ";" + resource segment id -> segment
		if (result.getSegments() == null) {
			result.setSegments(new HashMap<>());
		}
		if (result.getTripContainers() == null) {
			result.setTripContainers(new ArrayList<>());
		}
	}
	
	/**
	 * Создает мапинг словарей ответа.
	 */
	public void mapDictionaries(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		
		// мапим словари
		mappingGeo(request, searchResponse.getLocalities(), result.getLocalities());
		mappingObjects(request, searchResponse.getOrganisations(), result.getOrganisations(), MapType.ORGANIZATION,
				(mapping, lang, original) -> createOrganisation(mapping, lang, original),
				(original) -> UnmappingConverter.createUnmappingOrganisation(original),
				(resourceId, id, o) -> o.setId(getKey(resourceId, id)));
		mappingObjects(request, searchResponse.getVehicles(), result.getVehicles(), MapType.VEHICLE,
				(mapping, lang, original) -> DataConverter.createVehicle(mapping, lang, original),
				(original) -> UnmappingConverter.createUnmappingVehicle(original), 
				(resourceId, id, v) -> v.setId(getKey(resourceId, id)));
	}
	
	/**
	 * Получает мапинг словаря географии ответа и создает словарь из мапинга. Если мапинга нет, то добавляется объект ответа.
	 */
	public void mappingGeo(LangRequest request, Map<String, Locality> objects, Map<String, Locality> result) {
		
		// родителей тоже нужно попытаться смапить
		if (objects != null) {
			long resourceId = Long.parseLong(request.getParams().getResource().getId());
			List<Locality> localities = new ArrayList<>(objects.values());
			for (Locality locality : localities) {
				Locality parent = null;
				while ((parent = locality.getParent()) != null) {
					objects.put(parent.getId(), parent);
					locality.setParent(new Locality(getKey(resourceId, parent.getId())));
					locality = parent;
				}
			}
		}
		mappingObjects(request, objects, result, MapType.GEO,
				(mapping, lang, original) -> createLocality(mapping, lang, original),
				(original) -> UnmappingConverter.createUnmappingLocality(original),
				(resId, id, l) -> l.setId(getKey(resId, id)));
		List<Locality> localities = new ArrayList<>(result.values());
		for (Locality locality : localities) {
			fillMapByParents(locality, result);
			Locality parent = null;
			while ((parent = locality.getParent()) != null) {
				removeUnselectedLang(request, parent);
				locality = parent;
			}
		}
	}
	
	/*
	 * из-за того, что не все возвращаемые пункты ресурса смаплены, необходимо
	 * проверять маппинг родителей, если они есть
	 */
	private Locality createLocality(Mapping mapping, Lang lang, Locality original) {
		Locality locality = DataConverter.createLocality(mapping, lang, original);
		Locality result = locality;
		while ((mapping = mapping.getParent()) != null) {
			locality.setParent(DataConverter.createLocality(mapping, lang, original));
			locality = locality.getParent();
		}
		return result;
	}
	
	private void fillMapByParents(Locality locality, Map<String, Locality> result) {
		Locality parent = locality.getParent();
		if (parent != null) {
			if (parent.getParent() != null) {
				fillMapByParents(parent, result);
			}
			result.putIfAbsent(parent.getId(), parent);
			locality.setParent(new Locality(result.get(parent.getId()).getId()));
		}
	}
	
	/**
	 * Получает мапинг словарей ответа и создает словари из мапинга. Если мапинга нет, то добавляется объект ответа.
	 */
	public <T> void mappingObjects(LangRequest request, Map<String, T> objects, Map<String, T> result,
			MapType mapType, MapObjectCreator<T> creator, UnmappingCreator<T> unmappingCreator, ObjectIdSetter<T> idSetter) {
		if (objects == null
				|| objects.isEmpty()) {
			return;
		}
		long resourceId = Long.parseLong(request.getParams().getResource().getId());
		for (Entry<String, T> object : objects.entrySet()) {
			
			// получаем смапленную сущность
			List<Mapping> mappings = mappingService.getMappings(mapType, resourceId, object.getKey(), request.getLang());
			
			// добавляем сущность в мапу под ключем ид ресурса + ид обьекта,
			// чтобы от разных ресурсов не пересекались ид
			if (mappings == null) {
				if (object.getValue() != null) {
					
					// сохраняем несмапленные данные
					Unmapping unmapping = unmappingCreator.create(object.getValue());
					unmapping.setResourceMapId(object.getKey());
					unmapping.setResourceId(resourceId);
					unmapping.setType(mapType);
					mappingService.saveUnmapping(unmapping);
					
					T value = object.getValue();
					idSetter.set(resourceId, object.getKey(), value);
					result.put(getKey(resourceId, object.getKey()), value);
					
					// удаляем данные на языках, которые не запрашиваются
					removeUnselectedLang(request, value);
				}
			} else {
				result.put(getKey(resourceId, object.getKey()), creator.create(mappings.get(0), request.getLang(), object.getValue()));
			}
		}
	}
	
	private <T> void removeUnselectedLang(LangRequest request, T value) {
		if (request.getLang() != null) {
			if (value instanceof Name) {
				Name name = (Name) value;
				if (name.getName() != null
						&& name.getName().get(request.getLang()) != null) {
					name.getName().keySet().removeIf(l -> l != request.getLang());
				}
			}
			if (value instanceof Address) {
				Address address = (Address) value;
				if (address.getAddress() != null
						&& address.getAddress().get(request.getLang()) != null) {
					address.getAddress().keySet().removeIf(l -> l != request.getLang());
				}
			}
		}
	}
	
	/**
	 * Ключ словаря с ид ресурса + ид объекта словаря.
	 */
	public String getKey(long resourceId, String id) {
		return resourceId + ";" + id;
	}
	
	private Organisation createOrganisation(Mapping mapping, Lang lang, Organisation original) {
		com.gillsoft.ms.entity.Organisation org = dataController.getMappedOrganisation(mapping.getId());
		if (org == null) {
			return DataConverter.createOrganisation(mapping, lang, original);
		} else {
			return DataConverter.convert(org);
		}
	}
	
	public void updateSegments(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		updateSegments(request, searchResponse, result, true);
	}
	
	/**
	 * Проставляет мапинг всех объектов рейса, валидирует поля рейса и дополняет данными.
	 */
	public void updateSegments(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result, boolean onlyCalculated) {
		if (searchResponse.getSegments() == null) {
			return;
		}
		long resourceId = Long.parseLong(request.getParams().getResource().getId());
		result.getResources().put(String.valueOf(resourceId), request.getParams().getResource());
		for (Entry<String, Segment> entry : searchResponse.getSegments().entrySet()) {
			try {
				Segment segment = entry.getValue();
				
				// устанавливаем ресурс
				segment.setResource(new Resource(String.valueOf(resourceId)));
				
				// устанавливаем ид пунктов с маппинга
				segment.setDeparture(result.getLocalities().get(
						getKey(resourceId, segment.getDeparture().getId())));
				segment.setArrival(result.getLocalities().get(
						getKey(resourceId, segment.getArrival().getId())));
				
				String tripNumber = MappingService.getResourceTripNumber(segment, resourceId);
				
				// если транспорта нет, то добавляем его с маппинга по уникальному номеру рейса
				if (segment.getVehicle() == null
						&& !result.getVehicles().containsKey(tripNumber)) {
					Map<String, Vehicle> vehicles = new HashMap<>();
					vehicles.put(tripNumber, null);
					mappingObjects(request, vehicles, result.getVehicles(), MapType.VEHICLE,
							(mapping, lang, original) -> DataConverter.createVehicle(mapping, lang, original),
							(original) -> UnmappingConverter.createUnmappingVehicle(original),
							null);
				}
				// устанавливаем транспорт с маппинга
				String vehicleKey = segment.getVehicle() != null ? getKey(resourceId, segment.getVehicle().getId()) : tripNumber;
				if (result.getVehicles().containsKey(vehicleKey)) {
					segment.setVehicle(result.getVehicles().get(vehicleKey));
				}
				// если перевозчика нет, то добавляем его с маппинга по уникальному номеру рейса
				if (segment.getCarrier() == null
						&& !result.getOrganisations().containsKey(tripNumber + "_carrier")) {
					addOrganisationByTripNumber(tripNumber + "_carrier", result, request, MapType.ORGANIZATION);
				}
				// устанавливаем перевозчика с маппинга
				String carrierKey = segment.getCarrier() != null ? getKey(resourceId, segment.getCarrier().getId()) : tripNumber + "_carrier";
				if (result.getOrganisations().containsKey(carrierKey)) {
					segment.setCarrier(result.getOrganisations().get(carrierKey));
				}
				// если страховой нет, то добавляем её с маппинга по уникальному номеру рейса
				if (segment.getInsurance() == null
						&& !result.getOrganisations().containsKey(tripNumber + "_insurance")) {
					addOrganisationByTripNumber(tripNumber + "_insurance", result, request, MapType.ORGANIZATION);
				}
				// устанавливаем страховую с маппинга
				String insuranceKey = segment.getInsurance() != null ? getKey(resourceId, segment.getInsurance().getId()) : tripNumber + "_insurance";
				if (result.getOrganisations().containsKey(insuranceKey)) {
					segment.setInsurance(result.getOrganisations().get(insuranceKey));
				}
				addInsuranceCompensation(segment);
				
				// мапинг пунктов маршрута
				if (segment.getRoute() != null) {
					for (RoutePoint point : segment.getRoute().getPath()) {
						point.setLocality(new Locality(result.getLocalities().get(
								getKey(resourceId, point.getLocality().getId())).getId()));
					}
				}
				// доавляем ресурс в ид вагона
				if (segment.getCarriages() != null) {
					segment.getCarriages().forEach(c -> c.setId(new IdModel(resourceId, c.getId()).asString()));
				}
				// TODO мапинг тарифа
				
				// начисление сборов
				try {
					segment.setPrice(dataController.recalculate(segment, segment.getPrice(), request.getCurrency()));
					applyLang(segment.getPrice().getTariff(), request.getLang());
				} catch (Exception e) {
					if (onlyCalculated) {
						continue;
					}
				}
				// добавляем рейсы в результат
				result.getSegments().put(new IdModel(resourceId, entry.getKey()).asString(), segment);
			} catch (Exception e) {
				LOGGER.error("Can not map segment " + entry.getKey(), e);
			}
		}
		updateContainers(request, resourceId, result, searchResponse.getTripContainers());
		
		// проставляем ид словарей с маппинга
		updateDictionaries(result);
		
		// проставляем время в пути
		for (Segment segment : result.getSegments().values()) {
			setTimeInWay(segment);
		}
	}
	
	private void addInsuranceCompensation(Segment segment) {
		if (segment.getInsurance() != null) {
			if (segment.getInsurance().getProperties() == null) {
				segment.getInsurance().setProperties(new ConcurrentHashMap<>());
			}
			if (!segment.getInsurance().getProperties().containsKey("insurance_compensation")) {
				segment.getInsurance().getProperties().put("insurance_compensation", "102000");
			}
		}
	}
	
	public void applyLang(Tariff tariff, Lang lang) {
		if (tariff != null
				&& lang != null) {
			if (tariff.getDescription() != null
					&& tariff.getDescription().get(lang) != null) {
				tariff.getDescription().keySet().removeIf(k -> k != lang);
			}
			if (tariff.getName() != null
					&& tariff.getName().get(lang) != null) {
				tariff.getName().keySet().removeIf(k -> k != lang);
			}
			if (tariff.getReturnConditions() != null) {
				tariff.getReturnConditions().forEach(r -> applyLang(r, lang));
			}
		}
	}
	
	public void applyLang(ReturnCondition returnCondition, Lang lang) {
		if (returnCondition != null
				&& lang != null) {
			if (returnCondition.getTitle() != null
					&& returnCondition.getTitle().get(lang) != null) {
				returnCondition.getTitle().keySet().removeIf(k -> k != lang);
			}
			if (returnCondition.getDescription() != null
					&& returnCondition.getDescription().get(lang) != null) {
				returnCondition.getDescription().keySet().removeIf(k -> k != lang);
			}
		}
	}
	
	/*
	 * Обновляет ид рейсов и запросы.
	 */
	private void updateContainers(TripSearchRequest request, long resourceId, TripSearchResponse result, List<TripContainer> containers) {
		if (containers != null) {
			for (TripContainer container : containers) {
						
				// обновляем ид рейсов, а потом подменяем запрос
				updateTripIds(resourceId, result, container);
				
				// смапленный запрос
				container.setRequest(request);
				
				result.getTripContainers().add(container);
			}
		}
	}
	
	/*
	 * Объединяет все запросы с одинаковым мапингом.
	 */
	private void joinContainers(TripSearchResponse result) {
		if (result.getTripContainers() != null) {
			List<TripContainer> containers = new ArrayList<>();
			for (TripContainer container : result.getTripContainers()) {
						
				TripContainer resultContainer = getTripContainer(container.getRequest(), containers);
				if (resultContainer != null) {
					if (container.getTrips() != null) {
						if (resultContainer.getTrips() == null) {
							resultContainer.setTrips(container.getTrips());
						} else {
							resultContainer.getTrips().addAll(container.getTrips());
						}
					}
				} else {
					container.getRequest().setId(null);
					container.getRequest().setParams(null);
					containers.add(container);
				}
			}
			result.setTripContainers(containers);
		}
	}
	
	// Добавляем в ид рейса запрос, по которому он был найден.
	private void updateTripIds(long resourceId, TripSearchResponse result, TripContainer container) {
		if (container.getTrips() == null) {
			return;
		}
		for (Trip trip : container.getTrips()) {
			
			// обновляем прямые рейсы
			if (trip.getId() != null) {
				trip.setId(replaceTripId(new TripIdModel(resourceId, trip.getId(), container.getRequest()).asString(),
						new IdModel(resourceId, trip.getId()).asString(), result.getSegments()));
			}
			// обновляем обратные рейсы
			if (trip.getBackId() != null) {
				trip.setBackId(replaceTripId(new TripIdModel(resourceId, trip.getBackId(), container.getRequest()).asString(), 
						new IdModel(resourceId, trip.getBackId()).asString(), result.getSegments()));
			}
			// обновляем стыковочные рейсы
			if (trip.getSegments() != null) {
				trip.setSegments(trip.getSegments().stream().map(id ->
						replaceTripId(new TripIdModel(resourceId, id, container.getRequest()).asString(),
								new IdModel(resourceId, id).asString(), result.getSegments())).collect(Collectors.toList()));
			}
		}
	}
	
	private String replaceTripId(String newId, String oldId, Map<String, Segment> segments) {
		if (segments.containsKey(oldId)) {
			segments.put(newId, segments.remove(oldId));
		}
		return newId;
	}
	
	/*
	 * Проверяет и создает маппинг организации по номеру рейса.
	 */
	private void addOrganisationByTripNumber(String key, TripSearchResponse result, TripSearchRequest request, MapType mapType) {
		Map<String, Organisation> organisations = new HashMap<>();
		organisations.put(key, null);
		mappingObjects(request, organisations, result.getOrganisations(), MapType.ORGANIZATION,
				(mapping, lang, original) -> createOrganisation(mapping, lang, original),
				(original) -> UnmappingConverter.createUnmappingOrganisation(original),
				null);
	}
	
	/*
	 * Возвращает контейнер с таким же запросом либо null
	 */
	private TripContainer getTripContainer(TripSearchRequest request, List<TripContainer> containers) {
		for (TripContainer container : containers) {
			String[] requestPair = request.getLocalityPairs().get(0);
			String[] pair = container.getRequest().getLocalityPairs().get(0);
			if (Objects.equals(pair[0], requestPair[0])
					&& Objects.equals(pair[1], requestPair[1])
					&& Objects.equals(request.getDates().get(0), container.getRequest().getDates().get(0))
					&& ((request.getBackDates() == null && container.getRequest().getBackDates() == null)
							|| (Objects.equals(request.getBackDates().get(0), container.getRequest().getBackDates().get(0))))) {
				return container;
			}
		}
		return null;
	}
	
	/*
	 * Время в пути с учетом таймзон
	 */
	private void setTimeInWay(Segment segment) {
		try {
			segment.setTimeInWay(Utils.getTimeInWay(segment.getDepartureDate(), segment.getArrivalDate(),
					Utils.getLocalityTimeZone(segment.getDeparture().getId()),
					Utils.getLocalityTimeZone(segment.getArrival().getId())));
		} catch (NumberFormatException e) {
		}
	}
	
	/**
	 * Словарям поиска проставляет ид маппинга и убирает ид с самих объектов.
	 */
	public void updateResultDictionaries(TripSearchResponse result) {
		if (result.getSegments().isEmpty()) {
			result.setSegments(null);
		} else {
			for (Segment segment : result.getSegments().values()) {
				segment.setDeparture(new Locality(segment.getDeparture().getId()));
				segment.setArrival(new Locality(segment.getArrival().getId()));
				segment.setVehicle(segment.getVehicle() != null ? new Vehicle(segment.getVehicle().getId()) : null);
				segment.setCarrier(segment.getCarrier() != null ? new Organisation(segment.getCarrier().getId()) : null);
				segment.setInsurance(segment.getInsurance() != null ? new Organisation(segment.getInsurance().getId()) : null);
				// ресурс не проставляем так как там уже как надо
			}
		}
		if (result.getLocalities() != null) {
			if (result.getLocalities().isEmpty()) {
				result.setLocalities(null);
			} else {
				result.getLocalities().values().forEach(l -> l.setId(null));
			}
		}
		if (result.getOrganisations() != null) {
			if (result.getOrganisations().isEmpty()) {
				result.setOrganisations(null);
			} else {
				result.getOrganisations().values().forEach(o -> o.setId(null));
			}
		}
		if (result.getResources() != null) {
			if (result.getResources().isEmpty()) {
				result.setResources(null);
			} else {
				result.getResources().values().forEach(r -> r.setId(null));
			}
		}
		if (result.getVehicles() != null) {
			if (result.getVehicles().isEmpty()) {
				result.setVehicles(null);
			} else {
				result.getVehicles().values().forEach(v -> v.setId(null));
			}
		}
		if (result.getTripContainers().isEmpty()) {
			result.setTripContainers(null);
		}
		joinContainers(result);
	}
	
	private void updateDictionaries(TripSearchResponse result) {
		if (result.getLocalities() != null
				&& !result.getLocalities().isEmpty()) {
			result.setLocalities(result.getLocalities().values().stream().collect(Collectors.toMap(Locality::getId, l -> l, (l1, l2) -> l1)));
		}
		if (result.getOrganisations() != null
				&& !result.getOrganisations().isEmpty()) {
			result.setOrganisations(result.getOrganisations().values().stream().collect(Collectors.toMap(Organisation::getId, o -> o, (o1, o2) -> o1)));
		}
		if (result.getVehicles() != null
				&& !result.getVehicles().isEmpty()) {
			result.setVehicles(result.getVehicles().values().stream().collect(Collectors.toMap(Vehicle::getId, v -> v, (v1, v2) -> v1)));
		}
	}
	
	private interface MapObjectCreator<T> {
		
		public T create(Mapping mapping, Lang lang, T original);
		
	}
	
	private interface ObjectIdSetter<T> {
		
		public void set(long resourceId, String id, T object);
		
	}
	
	private interface UnmappingCreator<T> {
		
		public Unmapping create(T original);
		
	}

}
