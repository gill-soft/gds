package com.gillsoft.control.core;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.gillsoft.ms.entity.AdditionalServiceItem;
import com.gillsoft.ms.entity.ValueType;

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
		// resourceId + ";" + resource additional service id -> additional service
		if (result.getAdditionalServices() == null) {
			result.setAdditionalServices(new HashMap<>());
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
		
		Map<String, List<Organisation>> intermediateOrgResult = new HashMap<>();
		MappingCreator.organisationMappingCreator(
				request, searchResponse.getOrganisations(), intermediateOrgResult).mappingObjects(mappingService);
		addResult(result.getOrganisations(), intermediateOrgResult);
		
		Map<String, List<Vehicle>> intermediateVehicleResult = new HashMap<>();
		MappingCreator.vehicleMappingCreator(
				request, searchResponse.getVehicles(), intermediateVehicleResult).mappingObjects(mappingService);
		addResult(result.getVehicles(), intermediateVehicleResult);
	}
	
	/**
	 * Получает мапинг словаря географии ответа и создает словарь из мапинга. Если мапинга нет, то добавляется объект ответа.
	 */
	public void mappingGeo(LangRequest request, Map<String, Locality> objects, Map<String, Locality> result) {
		
		// родителей тоже нужно попытаться смапить
		if (objects != null) {
			List<Locality> localities = new ArrayList<>(objects.values());
			for (Locality locality : localities) {
				Locality parent = null;
				while ((parent = locality.getParent()) != null) {
					if (!objects.containsKey(parent.getId())) {
						objects.put(parent.getId(), parent);
					}
					locality = parent;
				}
			}
		}
		Map<String, List<Locality>> intermediateResult = new HashMap<>();
		MappingCreator<Locality> creator = MappingCreator.localityMappingCreator(request, objects, intermediateResult);
		creator.mappingObjects(mappingService);
		addResult(result, intermediateResult);
		
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
			locality.setParent(new Locality(parent.getId()));
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
	
	public void mapSegmentsTripId(Map<String, Locality> localities, Map<String, Segment> segmentsOfResource) {
		TripSearchRequest request = new TripSearchRequest();
		ResourceParams params = new ResourceParams();
		params.setResource(segmentsOfResource.values().iterator().next().getResource());
		request.setParams(params);
		segmentsOfResource.keySet().removeIf(key -> segmentsOfResource.get(key).getTripId() != null);
		Map<String, List<Segment>> fromMapping = mapSegments(new TripSearchResponse(), request, segmentsOfResource);
		long resourceId = MappingCreator.getResourceId(request);
		for (Segment segment : segmentsOfResource.values()) {
			String tripNumber = MappingService.getResourceTripNumber(segment, resourceId);
			String segmentKey = getKey(resourceId, tripNumber);
			setTripId(localities, segment, fromMapping, segmentKey);
		}
	}
	
	private Map<String, List<Segment>> mapSegments(TripSearchResponse searchResponse, TripSearchRequest request,
			Map<String, Segment> responseSegments) {
		long resourceId = MappingCreator.getResourceId(request);
		Map<String, List<Segment>> result = mapSegments(resourceId, searchResponse, request, responseSegments);
		result.putAll(getMappedSegments(resourceId, responseSegments));
		return result;
	}
	
	private Map<String, List<Segment>> mapSegments(long resourceId, TripSearchResponse searchResponse,
			TripSearchRequest request, Map<String, Segment> responseSegments) {
		Map<String, Segment> segments = responseSegments.values().stream().filter(s -> Objects.isNull(s.getTripId())).collect(
				Collectors.toMap(s -> MappingService.getResourceTripNumber(s, resourceId), s -> s, (s1, s2) -> s1));
		Map<String, List<Segment>> result = new HashMap<>();
		MappingCreator.segmentMappingCreator(searchResponse, request, segments, result).mappingObjects(mappingService);
		return result;
	}
	
	private Map<String, List<Segment>> getMappedSegments(long resourceId, Map<String, Segment> responseSegments) {
		Map<String, Segment> segments = responseSegments.values().stream().filter(s -> Objects.nonNull(s.getTripId())).collect(
				Collectors.toMap(s -> MappingService.getResourceTripNumber(s, resourceId), s -> s, (s1, s2) -> s1));
		return segments.values().stream().collect(
				Collectors.toMap(s -> getKey(resourceId, MappingService.getResourceTripNumber(s, resourceId)),
						s -> Arrays.asList(s.getTripId().split(";")).stream()
						.map(tripId -> DataConverter.createSegment(tripId, s)).collect(Collectors.toList()), (s1, s2) -> s1));
	}
	
	private void setTripId(Map<String, Locality> localities, Segment segment, Map<String, List<Segment>> fromMapping, String segmentKey) {
		if (fromMapping != null
				&& fromMapping.containsKey(segmentKey)) {
			List<Segment> segments = fromMapping.get(segmentKey);
			DataConverter.addMappedTrips(localities, segment, segments.stream().filter(s -> s.getTripId() != null)
					.map(s -> dataController.getTrip(Long.parseLong(s.getTripId()))).filter(Objects::nonNull).collect(Collectors.toList()));
		}
	}
	
	/**
	 * Проставляет мапинг всех объектов рейса, валидирует поля рейса и дополняет данными.
	 */
	public void updateSegments(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result,
			boolean onlyCalculated) {
		if (searchResponse.getSegments() == null) {
			return;
		}
		Map<String, List<Segment>> fromMapping = mapSegments(searchResponse, request, searchResponse.getSegments());
		long resourceId = MappingCreator.getResourceId(request);
		result.getResources().put(String.valueOf(resourceId), request.getParams().getResource());
		for (Entry<String, Segment> entry : searchResponse.getSegments().entrySet()) {
			try {
				Segment segment = entry.getValue();
				
				String tripNumber = MappingService.getResourceTripNumber(segment, resourceId);
				segment.setTripRresourceId(tripNumber);
				String segmentKey = getKey(resourceId, tripNumber);
				
				// устанавливаем ресурс
				segment.setResource(new Resource(String.valueOf(resourceId)));
				
				// устанавливаем ид пунктов с маппинга
				segment.setDeparture(result.getLocalities().get(
						getKey(resourceId, segment.getDeparture().getId())));
				segment.setArrival(result.getLocalities().get(
						getKey(resourceId, segment.getArrival().getId())));
				
				setTripId(result.getLocalities(), segment, fromMapping, segmentKey);
				
				// если транспорта нет, то добавляем его с маппинга по уникальному номеру рейса
				if (segment.getVehicle() == null
						&& !result.getVehicles().containsKey(tripNumber)) {
					Map<String, Vehicle> vehicles = new HashMap<>();
					vehicles.put(tripNumber, null);
					Map<String, List<Vehicle>> intermediateResult = new HashMap<>();
					MappingCreator.vehicleMappingCreator(
							request, vehicles, intermediateResult).mappingObjects(mappingService);
					addResult(result.getVehicles(), intermediateResult);
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
				// добавление допуслуг
				addAdditionalServices(request, result, segment);
				
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
		Map<String, String> timezones = new HashMap<>();
		for (Segment segment : result.getSegments().values()) {
			setTimeInWay(segment, timezones);
		}
	}
	
	private void addAdditionalServices(TripSearchRequest request, TripSearchResponse result, Segment segment) {
		List<AdditionalServiceItem> additionalServices = dataController.getAdditionalServices(segment);
		if (additionalServices != null) {
			for (AdditionalServiceItem additionalServiceItem : additionalServices) {
				if (additionalServiceItem.getValueType() == ValueType.PERCENT) {
					additionalServiceItem.setValue(segment.getPrice().getTariff().getValue()
							.multiply(additionalServiceItem.getValue().multiply(new BigDecimal(0.01))));
					
				}
			}
			if (segment.getAdditionalServices() == null) {
				segment.setAdditionalServices(new ArrayList<>());
			}
			segment.getAdditionalServices().addAll(additionalServices.stream().map(as -> DataConverter.convert(as)).collect(Collectors.toList()));
			segment.getAdditionalServices().forEach(as -> {
				applyLang(as, request.getLang());
				dataController.recalculate(as, as.getPrice(), request.getCurrency());
				as.setId(new IdModel(-1, as.getId()).asString());
				result.getAdditionalServices().put(as.getId(), as);
			});
		}
	}
	
	/*
	 * Проверяет и создает маппинг организации по номеру рейса.
	 */
	private void addOrganisationByTripNumber(String key, TripSearchResponse result, TripSearchRequest request, MapType mapType) {
		Map<String, Organisation> organisations = new HashMap<>();
		organisations.put(key, null);
		Map<String, List<Organisation>> intermediateResult = new HashMap<>();
		MappingCreator.organisationMappingCreator(
				request, organisations, intermediateResult).mappingObjects(mappingService);
		addResult(result.getOrganisations(), intermediateResult);
	}
	
	private <T> void addResult(Map<String, T> result, Map<String, List<T>> mappedResult) {
		for (Entry<String, List<T>> entry : mappedResult.entrySet()) {
			if (entry.getValue() != null
					&& entry.getValue().size() > 0) {
				result.put(entry.getKey(), entry.getValue().get(0));
			}
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
	
	public void applyLang(com.gillsoft.model.AdditionalServiceItem additionalService, Lang lang) {
		if (additionalService != null
				&& lang != null) {
			if (additionalService.getName() != null
					&& additionalService.getName().get(lang) != null) {
				additionalService.getName().keySet().removeIf(k -> k != lang);
			}
			if (additionalService.getDescription() != null
					&& additionalService.getDescription().get(lang) != null) {
				additionalService.getDescription().keySet().removeIf(k -> k != lang);
			}
			applyLang(additionalService.getPrice().getTariff(), lang);
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
			result.setLocalities(result.getLocalities().values().stream()
					.filter(l -> l.getId() != null).collect(Collectors.toMap(Locality::getId, l -> l, (l1, l2) -> l1)));
		}
		if (result.getOrganisations() != null
				&& !result.getOrganisations().isEmpty()) {
			result.setOrganisations(result.getOrganisations().values().stream()
					.filter(o -> o.getId() != null).collect(Collectors.toMap(Organisation::getId, o -> o, (o1, o2) -> o1)));
		}
		if (result.getVehicles() != null
				&& !result.getVehicles().isEmpty()) {
			result.setVehicles(result.getVehicles().values().stream()
					.filter(v -> v.getId() != null).collect(Collectors.toMap(Vehicle::getId, v -> v, (v1, v2) -> v1)));
		}
		if (result.getAdditionalServices() != null
				&& !result.getAdditionalServices().isEmpty()) {
			result.setAdditionalServices(result.getAdditionalServices().values().stream()
					.filter(as -> as.getId() != null).collect(Collectors.toMap(com.gillsoft.model.AdditionalServiceItem::getId, as -> as, (as1, as2) -> as1)));
		}
	}
	
	/*
	 * Время в пути с учетом таймзон
	 */
	private void setTimeInWay(Segment segment, Map<String, String> timezones) {
		try {
			segment.setTimeInWay(Utils.getTimeInWay(segment.getDepartureDate(), segment.getArrivalDate(),
					getTimeZone(segment.getDeparture().getId(), timezones),
					getTimeZone(segment.getArrival().getId(), timezones)));
		} catch (NumberFormatException e) {
		}
	}
	
	private String getTimeZone(String localityId, Map<String, String> timezones) {
		if (timezones.containsKey(localityId)) {
			return timezones.get(localityId);
		} else {
			String timezone = Utils.getLocalityTimeZone(localityId);
			timezones.put(localityId, timezone);
			return timezone;
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
