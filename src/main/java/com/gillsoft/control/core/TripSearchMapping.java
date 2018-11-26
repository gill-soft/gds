package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TripSearchMapping {
	
	private static Logger LOGGER = LogManager.getLogger(TripSearchMapping.class);
	
	@Autowired
	private LocalityController localityController;
	
	@Autowired
	private MappingService mappingService;
	
	/**
	 * Создает пустые словари в ответе.
	 */
	public void createDictionaries(TripSearchResponse result) {
		
		// resource locality id -> locality from mapping
		result.setLocalities(new HashMap<>());
		
		// resource locality id -> locality from mapping
		result.setOrganisations(new HashMap<>());
		
		// resource locality id -> locality from mapping
		result.setVehicles(new HashMap<>());
		
		// resourceId + ";" + resource segment id -> segment
		result.setSegments(new HashMap<>());
		
		result.setTripContainers(new ArrayList<>());
	}
	
	/**
	 * Создает мапинг словарей ответа.
	 */
	public void mapDictionaries(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		
		// мапим словари
		mappingObjects(request, searchResponse.getLocalities(), result.getLocalities(), MapType.GEO,
				(mapping, lang) -> localityController.createLocality(mapping, lang),
				(resourceId, id, l) -> l.setId(getKey(resourceId, id)));
		mappingObjects(request, searchResponse.getOrganisations(), result.getOrganisations(), MapType.CARRIER,
				(mapping, lang) -> createOrganisation(mapping, lang),
				(resourceId, id, o) -> o.setId(getKey(resourceId, id)));
		mappingObjects(request, searchResponse.getVehicles(), result.getVehicles(), MapType.BUS,
				(mapping, lang) -> createVehicle(mapping, lang),
				(resourceId, id, v) -> v.setId(getKey(resourceId, id)));
	}
	
	/**
	 * Получает мапинг словарей ответа и создает словари из мапинга. Если мапинга нет, то добавляется объект ответа.
	 */
	public <T> void mappingObjects(TripSearchRequest request, Map<String, T> objects, Map<String, T> result,
			MapType mapType, MapObjectCreator<T> creator, ObjectIdSetter<T> idSetter) {
		if (objects == null
				|| objects.isEmpty()) {
			return;
		}
		long resourceId = request.getParams().getResource().getId();
		for (Entry<String, T> object : objects.entrySet()) {
			
			// получаем смапленную сущность
			List<Mapping> mappings = mappingService.getMappings(mapType, resourceId, object.getKey(), request.getLang());
			
			// добавляем сущность в мапу под ключем ид ресурса + ид обьекта,
			// чтобы от разных ресурсов не пересекались ид
			if (mappings == null) {
				if (object.getValue() != null) {
					idSetter.set(resourceId, object.getKey(), object.getValue());
					result.put(getKey(resourceId, object.getKey()), object.getValue());
				}
			} else {
				result.put(getKey(resourceId, object.getKey()), creator.create(mappings.get(0), request.getLang()));
			}
		}
	}
	
	/*
	 * Ключ словаря с ид ресурса + ид объекта словаря.
	 */
	private String getKey(long resourceId, String id) {
		return resourceId + ";" + id;
	}
	
	/*
	 * Создает организацию по данным мапинга.
	 */
	private Organisation createOrganisation(Mapping mapping, Lang lang) {
		Organisation organisation = new Organisation();
		organisation.setId(String.valueOf(mapping.getId()));
		if (lang == null
				&& mapping.getLangAttributes() != null) {
			for (Entry<Lang, ConcurrentMap<String, String>> entry : mapping.getLangAttributes().entrySet()) {
				organisation.setName(entry.getKey(), entry.getValue().get("NAME"));
				organisation.setAddress(entry.getKey(), entry.getValue().get("ADDRESS"));
			}
		} else if (mapping.getAttributes() != null) {
			organisation.setName(lang, mapping.getAttributes().get("NAME"));
			organisation.setAddress(lang, mapping.getAttributes().get("ADDRESS"));
		}
		if (mapping.getAttributes() != null) {
			organisation.setTradeMark(mapping.getAttributes().get("TRADEMARK"));
			if (mapping.getAttributes().containsKey("PHONES")) {
				organisation.setPhones(Arrays.asList(mapping.getAttributes().get("PHONES").split(";")));
			}
			if (mapping.getAttributes().containsKey("EMAILS")) {
				organisation.setEmails(Arrays.asList(mapping.getAttributes().get("EMAILS").split(";")));
			}
		}
		return organisation;
	}
	
	/*
	 * Создает транспорт по данным мапинга.
	 */
	private Vehicle createVehicle(Mapping mapping, Lang lang) {
		Vehicle vehicle = new Vehicle();
		vehicle.setId(String.valueOf(mapping.getId()));
		if (lang == null
				&& mapping.getLangAttributes() != null) {
			for (Entry<Lang, ConcurrentMap<String, String>> entry : mapping.getLangAttributes().entrySet()) {
				if (entry.getValue().get("MODEL") != null) {
					vehicle.setModel(entry.getValue().get("MODEL"));
				}
			}
		} else if (mapping.getAttributes() != null) {
			vehicle.setModel(mapping.getAttributes().get("MODEL"));
		}
		if (mapping.getAttributes() != null) {
			if (mapping.getAttributes().containsKey("CAPACITY")) {
				try {
					vehicle.setCapacity(Integer.valueOf(mapping.getAttributes().get("CAPACITY")));
				} catch (NumberFormatException e) {
					LOGGER.error("Invalid capacity for busmodel id: " + mapping.getId(), e);
				}
			}
			vehicle.setNumber(mapping.getAttributes().get("NUMBER"));
		}
		return vehicle;
	}
	
	/**
	 * Проставляет мапинг всех объектов рейса, валидирует поля рейса и дополняет данными.
	 */
	public void updateSegments(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		if (searchResponse.getSegments() == null) {
			return;
		}
		long resourceId = request.getParams().getResource().getId();
		for (Entry<String, Segment> entry : searchResponse.getSegments().entrySet()) {
			Segment segment = entry.getValue();
			
			// устанавливаем ид пунктов с маппинга
			segment.setDeparture(new Locality(result.getLocalities().get(
					getKey(resourceId, segment.getDeparture().getId())).getId()));
			segment.setArrival(new Locality(result.getLocalities().get(
					getKey(resourceId, segment.getArrival().getId())).getId()));
			
			String tripNumber = mappingService.getResourceTripNumber(segment, resourceId);
			
			// если транспорта нет, то добавляем его с маппинга по уникальному номеру рейса
			if (segment.getVehicle() == null
					&& !result.getVehicles().containsKey(tripNumber)) {
				Map<String, Vehicle> vehicles = new HashMap<>();
				vehicles.put(tripNumber, null);
				mappingObjects(request, vehicles, result.getVehicles(), MapType.BUS,
						(mapping, lang) -> createVehicle(mapping, lang), null);
			}
			// устанавливаем ид транспорта с маппинга
			String vehicleKey = segment.getVehicle() != null ? getKey(resourceId, segment.getVehicle().getId()) : tripNumber;
			if (result.getVehicles().containsKey(vehicleKey)) {
				segment.setVehicle(new Vehicle(result.getVehicles().get(vehicleKey).getId()));
			}
			// если перевозчика нет, то добавляем его с маппинга по уникальному номеру рейса
			if (segment.getCarrier() == null
					&& !result.getOrganisations().containsKey(tripNumber + "_carrier")) {
				addOrganisationByTripNumber(tripNumber + "_carrier", result, request, MapType.CARRIER);
			}
			// устанавливаем ид перевозчика с маппинга
			String carrierKey = segment.getCarrier() != null ? getKey(resourceId, segment.getCarrier().getId()) : tripNumber + "_carrier";
			if (result.getOrganisations().containsKey(carrierKey)) {
				segment.setCarrier(new Organisation(result.getOrganisations().get(carrierKey).getId()));
			}
			// если страховой нет, то добавляем его с маппинга по уникальному номеру рейса
			if (segment.getInsurance() == null
					&& !result.getOrganisations().containsKey(tripNumber + "_insurance")) {
				addOrganisationByTripNumber(tripNumber + "_insurance", result, request, MapType.INSURANCE);
			}
			// устанавливаем ид страховой с маппинга
			String insuranceKey = segment.getInsurance() != null ? getKey(resourceId, segment.getInsurance().getId()) : tripNumber + "_insurance";
			if (result.getOrganisations().containsKey(insuranceKey)) {
				segment.setInsurance(new Organisation(result.getOrganisations().get(insuranceKey).getId()));
			}
			// мапинг тарифа и начислени сборов TODO
			
			// добавляем рейсы в результат
			result.getSegments().put(new TripIdModel(resourceId, entry.getKey()).asString(), segment);
		}
		// объединяем контайнеры по смапленому запросу
		joinContainers(resourceId, result.getTripContainers(), searchResponse.getTripContainers());
		
		// добавляем ид ресурса к ид рейса
		updateTripIdsAndTimeInWay(resourceId, searchResponse);
	}
	
	/*
	 * Объединяет все запросы с одинаковым мапингом.
	 */
	private void joinContainers(long resourceId, List<TripContainer> result, List<TripContainer> containers) {
		for (TripContainer container : containers) {
			
			List<Mapping> fromMappings = mappingService.getMappings(MapType.GEO, resourceId,
					container.getRequest().getLocalityPairs().get(0)[0]);
			if (fromMappings != null) {
				List<Mapping> toMappings = mappingService.getMappings(MapType.GEO, resourceId,
						container.getRequest().getLocalityPairs().get(0)[1]);
				if (toMappings != null) {
					TripContainer resultContainer = getTripContainer(container.getRequest(), result);
					if (resultContainer != null) {
						resultContainer.getTrips().addAll(container.getTrips());
					} else {
						container.getRequest().setLocalityPairs(Collections.singletonList(
								new String[] { String.valueOf(fromMappings.get(0).getId()), String.valueOf(toMappings.get(0).getId())}));
						result.add(container);
					}
				}
			}
		}
	}
	
	/*
	 * Проверяет и создает маппинг организации по номеру рейса.
	 */
	private void addOrganisationByTripNumber(String key, TripSearchResponse result, TripSearchRequest request, MapType mapType) {
		Map<String, Organisation> organisations = new HashMap<>();
		organisations.put(key, null);
		mappingObjects(request, organisations, result.getOrganisations(), MapType.CARRIER,
				(mapping, lang) -> createOrganisation(mapping, lang), null);
	}
	
	/*
	 * Возвращает контейнер с таким же запросом либо null
	 */
	private TripContainer getTripContainer(TripSearchRequest request, List<TripContainer> result) {
		for (TripContainer container : result) {
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
	 * Добавляет к каждому ид рейса ид ресурса и устанавливает время в пути рейсам с учетом таймзон.
	 */
	private void updateTripIdsAndTimeInWay(long resourceId, TripSearchResponse searchResponse) {
		for (TripContainer container : searchResponse.getTripContainers()) {
			if (container.getTrips() != null) {
				String[] pair = container.getRequest().getLocalityPairs().get(0);
				for (Trip trip : container.getTrips()) {
					if (trip.getId() != null) {
						setTimeInWay(searchResponse.getSegments().get(trip.getId()), pair);
						trip.setId(new TripIdModel(resourceId, trip.getId()).asString());
					}
					if (trip.getBackId() != null) {
						setTimeInWay(searchResponse.getSegments().get(trip.getBackId()), pair);
						trip.setBackId(new TripIdModel(resourceId, trip.getBackId()).asString());
					}
					if (trip.getSegments() != null) {
						trip.getSegments().forEach((id) -> setTimeInWay(searchResponse.getSegments().get(id), pair));
						trip.setSegments(trip.getSegments().stream().map(id -> new TripIdModel(resourceId, id).asString()).collect(Collectors.toList()));
					}
				}
			}
		}
	}
	
	/*
	 * Время в пути с учетом таймзон
	 */
	private void setTimeInWay(Segment segment, String[] pair) {
		segment.setTimeInWay(Utils.getTimeInRoad(segment.getDepartureDate(), segment.getArrivalDate(),
				Long.valueOf(pair[0]), Long.valueOf(pair[1])));
	}
	
	/**
	 * Словарям поиска проставляет ид маппинга и убирает ид с самих объектов.
	 */
	public void updateResultDictionaries(TripSearchResponse result) {
		if (result.getLocalities() != null) {
			if (result.getLocalities().isEmpty()) {
				result.setLocalities(null);
			} else {
				result.setLocalities(result.getLocalities().values().stream().collect(Collectors.toMap(Locality::getId, l -> {
					l.setId(null);
					return l;
				}, (l1, l2) -> l1)));
			}
		}
		if (result.getOrganisations() != null) {
			if (result.getOrganisations().isEmpty()) {
				result.setOrganisations(null);
			} else {
				result.setOrganisations(result.getOrganisations().values().stream().collect(Collectors.toMap(Organisation::getId, o -> {
					o.setId(null);
					return o;
				}, (o1, o2) -> o1)));
			}
		}
		if (result.getVehicles() != null) {
			if (result.getVehicles().isEmpty()) {
				result.setVehicles(null);
			} else {
				result.setVehicles(result.getVehicles().values().stream().collect(Collectors.toMap(Vehicle::getId, v -> {
					v.setId(null);
					return v;
				}, (v1, v2) -> v1)));
			}
		}
	}
	
	private interface MapObjectCreator<T> {
		
		public T create(Mapping mapping, Lang lang);
		
	}
	
	private interface ObjectIdSetter<T> {
		
		public void set(long resourceId, String id, T object);
		
	}

}
