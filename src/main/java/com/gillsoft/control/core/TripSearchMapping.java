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
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
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
import com.gillsoft.model.request.ResourceParams;
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
		MappingCreator.organisationMappingCreator(
				request, searchResponse.getOrganisations(), result.getOrganisations()).mappingObjects(mappingService);
		MappingCreator.vehicleMappingCreator(
				request, searchResponse.getVehicles(), result.getVehicles()).mappingObjects(mappingService);
	}
	
	/**
	 * Получает мапинг словаря географии ответа и создает словарь из мапинга. Если мапинга нет, то добавляется объект ответа.
	 */
	public void mappingGeo(LangRequest request, Map<String, Locality> objects, Map<String, Locality> result) {
		
		// родителей тоже нужно попытаться смапить
		if (objects != null) {
			long resourceId = MappingCreator.getResourceId(request);
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
		MappingCreator<Locality> creator = MappingCreator.localityMappingCreator(request, objects, result);
		creator.mappingObjects(mappingService);
		List<Locality> localities = new ArrayList<>(result.values());
		for (Locality locality : localities) {
			fillMapByParents(locality, result);
			Locality parent = null;
			while ((parent = locality.getParent()) != null) {
				creator.removeUnselectedLang(parent);
				locality = parent;
			}
		}
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
	 * Ключ словаря с ид ресурса + ид объекта словаря.
	 */
	public String getKey(long resourceId, String id) {
		return MappingCreator.getKey(resourceId, id);
	}
	
	public void updateSegments(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		updateSegments(request, searchResponse, result, true);
	}
	
	public void mapSegmentsTripId(Map<String, Segment> responseSegments) {
		TripSearchRequest request = new TripSearchRequest();
		ResourceParams params = new ResourceParams();
		params.setResource(responseSegments.values().iterator().next().getResource());
		request.setParams(params);
		Map<String, Segment> fromMapping = mapSegments(new TripSearchResponse(), request, responseSegments);
		long resourceId = MappingCreator.getResourceId(request);
		for (Segment segment : responseSegments.values()) {
			String tripNumber = MappingService.getResourceTripNumber(segment, resourceId);
			String segmentKey = getKey(resourceId, tripNumber);
			if (fromMapping.containsKey(segmentKey)) {
				Segment s = fromMapping.get(segmentKey);
				if (s.getTripId() != null) {
					segment.setTripId(s.getTripId());
				}
			}
		}
	}
	
	private Map<String, Segment> mapSegments(TripSearchResponse searchResponse, TripSearchRequest request,
			Map<String, Segment> responseSegments) {
		long resourceId = MappingCreator.getResourceId(request);
		Map<String, Segment> segments = responseSegments.values().stream().collect(
				Collectors.toMap(s -> MappingService.getResourceTripNumber(s, resourceId), s -> s, (s1, s2) -> s1));
		Map<String, Segment> result = new HashMap<>();
		MappingCreator.segmentMappingCreator(searchResponse, request, segments, result).mappingObjects(mappingService);
		return result;
	}
	
	/**
	 * Проставляет мапинг всех объектов рейса, валидирует поля рейса и дополняет данными.
	 */
	public void updateSegments(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result, boolean onlyCalculated) {
		if (searchResponse.getSegments() == null) {
			return;
		}
		Map<String, Segment> fromMapping = mapSegments(searchResponse, request, searchResponse.getSegments());
		long resourceId = MappingCreator.getResourceId(request);
		result.getResources().put(String.valueOf(resourceId), request.getParams().getResource());
		for (Entry<String, Segment> entry : searchResponse.getSegments().entrySet()) {
			try {
				Segment segment = entry.getValue();
				String tripNumber = MappingService.getResourceTripNumber(segment, resourceId);
				segment.setTripRresourceId(tripNumber);
				String segmentKey = getKey(resourceId, tripNumber);
				if (fromMapping.containsKey(segmentKey)) {
					Segment s = fromMapping.get(segmentKey);
					if (s.getTripId() != null) {
						segment.setTripId(s.getTripId());
					}
				}
				// устанавливаем ресурс
				segment.setResource(new Resource(String.valueOf(resourceId)));
				
				// устанавливаем ид пунктов с маппинга
				segment.setDeparture(result.getLocalities().get(
						getKey(resourceId, segment.getDeparture().getId())));
				segment.setArrival(result.getLocalities().get(
						getKey(resourceId, segment.getArrival().getId())));
				
				// если транспорта нет, то добавляем его с маппинга по уникальному номеру рейса
				if (segment.getVehicle() == null
						&& !result.getVehicles().containsKey(tripNumber)) {
					Map<String, Vehicle> vehicles = new HashMap<>();
					vehicles.put(tripNumber, null);
					MappingCreator.vehicleMappingCreator(
							request, vehicles, result.getVehicles()).mappingObjects(mappingService);
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
	
	/*
	 * Проверяет и создает маппинг организации по номеру рейса.
	 */
	private void addOrganisationByTripNumber(String key, TripSearchResponse result, TripSearchRequest request, MapType mapType) {
		Map<String, Organisation> organisations = new HashMap<>();
		organisations.put(key, null);
		MappingCreator.organisationMappingCreator(
				request, organisations, result.getOrganisations()).mappingObjects(mappingService);
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
		result.fillMaps();
		if (result.getTripContainers().isEmpty()) {
			result.setTripContainers(null);
		}
		joinContainers(result);
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

}
