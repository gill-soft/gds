package com.gillsoft.control.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.MemoryCacheHandler;
import com.gillsoft.control.service.AgregatorTripSearchService;
import com.gillsoft.mapper.model.MapType;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Method;
import com.gillsoft.model.MethodType;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.ResponseError;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Trip;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.ms.entity.Resource;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TripSearchController {
	
	private static Logger LOGGER = LogManager.getLogger(TripSearchController.class);
	
	@Autowired
    @Qualifier("MemoryCacheHandler")
	private CacheHandler cache;
	
	@Autowired
	private AgregatorTripSearchService service;
	
	@Autowired
	private MsDataController dataController;
	
	@Autowired
	private ResourceInfoController infoController;
	
	@Autowired
	private LocalityController localityController;
	
	@Autowired
	private MappingService mappingService;
	
	public TripSearchResponse initSearch(TripSearchRequest request) {
		
		// проверяем параметры запроса TODO
		
		// проверяем ответ и записываем в память запросы под ид поиска
		List<TripSearchRequest> requests = createSearchRequest(request);
		TripSearchResponse response = service.initSearch(requests);
		if (response.getError() != null) {
			return response;
		}
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, response.getSearchId());
		params.put(MemoryCacheHandler.TIME_TO_LIVE, 300000l);
		try {
			cache.write(requests, params);
		} catch (IOCacheException e) {
		}
		return response;
	}
	
	@SuppressWarnings("unchecked")
	public TripSearchResponse getSearchResult(String searchId) {
		
		// получает запросы поиска с памяти по ид поиска и проверяем их
		Map<String, Object> params = new HashMap<>();
		params.put(MemoryCacheHandler.OBJECT_NAME, searchId);
		List<TripSearchRequest> requests = null;
		try {
			requests = (List<TripSearchRequest>) cache.read(params);
		} catch (ClassCastException | IOCacheException e) {
			LOGGER.error("Empty request by searchId: " + searchId, e);
		}
		if (requests == null) {
			LOGGER.error("Too late for getting result by searchId: " + searchId);
			return new TripSearchResponse(null, new ResponseError("Too late for getting result or invalid searchId."));
		}
		TripSearchResponse response = service.getSearchResult(searchId);
		if (response.getError() != null) {
			return response;
		}
		if (response.getResult() != null
				&& !response.getResult().isEmpty()) {
			
			TripSearchResponse result = new TripSearchResponse();
			result.setId(requests.get(0).getId().split(";")[0]);
			
			// создаем словари ответа
			createDictionaries(result);
			
			for (TripSearchResponse searchResponse : response.getResult()) {
				
				Stream<TripSearchRequest> stream = requests.stream().filter(request -> request.getId().equals(searchResponse.getId()));
				if (stream != null) {
					
					// запрос, по которому получен результат
					TripSearchRequest request = stream.findFirst().get();
					
					// проверяем ошибки
					if (searchResponse.getTripContainers() != null) {
						
						// логируем ошибки, если есть
						logError(searchResponse);
						
						// мапим словари
						mapDictionaries(request, searchResponse, result);
						
						// обновляем мапингом рейсы
						updateSegments(request, searchResponse, result);
					}
				}
			}
			// меняем ключи мап на ид из мапинга
			updateResultDictionaries(result);
		}
		return response;
	}
	
	private void createDictionaries(TripSearchResponse result) {
		
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
	
	private void logError(TripSearchResponse searchResponse) {
		for (TripContainer container : searchResponse.getTripContainers()) {
			if (container.getError() != null) {
				try {
					LOGGER.error("ERROR in response id: " + searchResponse.getId()
					+ "\n" + StringUtil.objectToJsonString(container.getError()));
				} catch (JsonProcessingException e) {
					LOGGER.error("ERROR in response id: " + container.getError());
				}
			}
		}
	}
	
	private void mapDictionaries(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		
		// мапим пункты ответа
		mappingObjects(request, searchResponse.getLocalities(), result.getLocalities(), MapType.GEO,
				(mapping, lang) -> localityController.createLocality(mapping, lang));
		
		// мапим организации
		mappingObjects(request, searchResponse.getOrganisations(), result.getOrganisations(), MapType.CARRIER,
				(mapping, lang) -> createOrganisation(mapping, lang));
		
		// мапим транспорт
		mappingObjects(request, searchResponse.getVehicles(), result.getVehicles(), MapType.BUS,
				(mapping, lang) -> createVehicle(mapping, lang));
	}
	
	private void updateResultDictionaries(TripSearchResponse result) {
		if (result.getLocalities().isEmpty()) {
			result.setLocalities(null);
		} else {
			result.setLocalities(result.getLocalities().values().stream().collect(Collectors.toMap(Locality::getId, l -> l)));
		}
		if (result.getOrganisations().isEmpty()) {
			result.setOrganisations(null);
		} else {
			result.setOrganisations(result.getOrganisations().values().stream().collect(Collectors.toMap(Organisation::getId, o -> o)));
		}
		if (result.getVehicles().isEmpty()) {
			result.setVehicles(null);
		} else {
			result.setVehicles(result.getVehicles().values().stream().collect(Collectors.toMap(Vehicle::getId, v -> v)));
		}
	}
	
	private void updateSegments(TripSearchRequest request, TripSearchResponse searchResponse, TripSearchResponse result) {
		if (searchResponse.getSegments() == null) {
			return;
		}
		long resourceId = request.getParams().getResource().getId();
		for (Segment segment : searchResponse.getSegments().values()) {
			
			// устанавливаем ид пунктов с маппинга
			if (result.getLocalities().containsKey(segment.getDeparture().getId())) {
				segment.setDeparture(new Locality(result.getLocalities().get(segment.getDeparture().getId()).getId()));
			}
			if (result.getLocalities().containsKey(segment.getArrival().getId())) {
				segment.setArrival(new Locality(result.getLocalities().get(segment.getArrival().getId()).getId()));
			}
			String tripNumber = mappingService.getResourceTripNumber(segment, resourceId);
			
			// если транспорта нет, то добавляем его с маппинга по уникальному номеру рейса
			if (segment.getVehicle() == null
					&& !result.getVehicles().containsKey(tripNumber)) {
				Map<String, Vehicle> vehicles = new HashMap<>();
				vehicles.put(tripNumber, null);
				mappingObjects(request, vehicles, result.getVehicles(), MapType.BUS,
						(mapping, lang) -> createVehicle(mapping, lang));
			}
			// устанавливаем ид транспорта с маппинга
			String vehicleKey = segment.getVehicle() != null ? segment.getVehicle().getId() : tripNumber;
			if (result.getVehicles().containsKey(vehicleKey)) {
				segment.setVehicle(new Vehicle(result.getVehicles().get(vehicleKey).getId()));
			}
			// если перевозчика нет, то добавляем его с маппинга по уникальному номеру рейса
			if (segment.getCarrier() == null
					&& !result.getOrganisations().containsKey(tripNumber + "_carrier")) {
				addOrganisationByTripNumber(tripNumber + "_carrier", result, request, MapType.CARRIER);
			}
			// устанавливаем ид перевозчика с маппинга
			String carrierKey = segment.getCarrier() != null ? segment.getCarrier().getId() : tripNumber + "_carrier";
			if (result.getOrganisations().containsKey(carrierKey)) {
				segment.setCarrier(new Organisation(result.getOrganisations().get(carrierKey).getId()));
			}
			// если страховой нет, то добавляем его с маппинга по уникальному номеру рейса
			if (segment.getInsurance() == null
					&& !result.getOrganisations().containsKey(tripNumber + "_insurance")) {
				addOrganisationByTripNumber(tripNumber + "_insurance", result, request, MapType.INSURANCE);
			}
			// устанавливаем ид страховой с маппинга
			String insuranceKey = segment.getInsurance() != null ? segment.getInsurance().getId() : tripNumber + "_insurance";
			if (result.getOrganisations().containsKey(insuranceKey)) {
				segment.setInsurance(new Organisation(result.getOrganisations().get(insuranceKey).getId()));
			}
			// время в пути с учетом таймзон TODO
			// мапинг тарифа и начислени сборов TODO
			
			// добавляем рейсы в результат
			result.getSegments().put(resourceId + ";" + segment.getId(), segment);
		}
		// добавляем ид ресурса к ид рейса
		updateTripIds(resourceId, searchResponse);
		
		// объединяем контайнеры по смапленому запросу
		joinContainers(resourceId, result.getTripContainers(), searchResponse.getTripContainers());
	}
	
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
	
	// возвращает контейнер с таким же запросом либо null
	private TripContainer getTripContainer(TripSearchRequest request, List<TripContainer> result) {
		for (TripContainer container : result) {
			String[] requestPair = request.getLocalityPairs().get(0);
			String[] pair = container.getRequest().getLocalityPairs().get(0);
			if (Objects.equals(pair[0], requestPair[0])
					&& Objects.equals(pair[1], requestPair[1])
					&& Objects.equals(request.getDates().get(0), container.getRequest().getDates().get(0))
					&& ((request.getBackDates() == null && container.getRequest().getBackDates() == null))
							|| (Objects.equals(request.getBackDates().get(0), container.getRequest().getBackDates().get(0)))) {
				return container;
			}
		}
		return null;
	}
	
	private void updateTripIds(long resourceId, TripSearchResponse searchResponse) {
		for (TripContainer container : searchResponse.getTripContainers()) {
			if (container.getTrips() != null) {
				for (Trip trip : container.getTrips()) {
					if (trip.getId() != null) {
						trip.setId(resourceId + ";" + trip.getId());
					}
					if (trip.getBackId() != null) {
						trip.setBackId(resourceId + ";" + trip.getBackId());
					}
					if (trip.getSegments() != null) {
						trip.setSegments(trip.getSegments().stream().map(id -> resourceId + ";" + id).collect(Collectors.toList()));
					}
				}
			}
		}
	}
	
	private void addOrganisationByTripNumber(String key, TripSearchResponse result, TripSearchRequest request, MapType mapType) {
		Map<String, Organisation> organisations = new HashMap<>();
		organisations.put(key, null);
		mappingObjects(request, organisations, result.getOrganisations(), MapType.CARRIER,
				(mapping, lang) -> createOrganisation(mapping, lang));
	}
	
	private <T> void mappingObjects(TripSearchRequest request, Map<String, T> objects, Map<String, T> result,
			MapType mapType, MapObjectCreator<T> creator) {
		if (objects == null
				|| objects.isEmpty()) {
			return;
		}
		for (Entry<String, T> object : objects.entrySet()) {
			
			// получаем смапленную сущность
			List<Mapping> mappings = mappingService.getMappings(mapType,
					request.getParams().getResource().getId(), object.getKey(), request.getLang());
			if (mappings == null) {
				result.put(object.getKey(), object.getValue());
			} else {
				result.put(object.getKey(), creator.create(mappings.get(0), request.getLang()));
			}
		}
	}
	
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
	
	private List<TripSearchRequest> createSearchRequest(TripSearchRequest searchRequest) {
		List<Resource> resources = dataController.getUserResources();
		if (resources != null) {
			List<TripSearchRequest> request = new ArrayList<>();
			for (Resource resource : resources) {
				if (infoController.isMethodAvailable(resource, Method.SEARCH, MethodType.POST)) {
					
					// проверяем маппинг и формируем запрос на каждый ресурс
					for (String[] pairs : searchRequest.getLocalityPairs()) {
						Set<String> fromIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pairs[0]));
						if (fromIds != null) {
							Set<String> toIds = mappingService.getResourceIds(resource.getId(), Long.parseLong(pairs[1]));
							if (toIds != null) {
								TripSearchRequest resourceSearchRequest = new TripSearchRequest();
								resourceSearchRequest.setId(searchRequest.getId() + ";" + StringUtil.generateUUID());
								resourceSearchRequest.setParams(resource.createParams());
								resourceSearchRequest.setDates(searchRequest.getDates());
								resourceSearchRequest.setBackDates(searchRequest.getBackDates());
								resourceSearchRequest.setCurrency(searchRequest.getCurrency());
								resourceSearchRequest.setLocalityPairs(new ArrayList<>());
								resourceSearchRequest.setLang(searchRequest.getLang());
								request.add(resourceSearchRequest);
								for (String fromId : fromIds) {
									for (String toId : toIds) {
										resourceSearchRequest.getLocalityPairs().add(new String[] { fromId, toId });
									}
								}
							}
						}
					}
				}
			}
			return request;
		}
		return null;
	}
	
	private interface MapObjectCreator<T> {
		
		public T create(Mapping mapping, Lang lang);
		
	}

}
