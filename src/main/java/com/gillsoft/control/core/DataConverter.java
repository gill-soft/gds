package com.gillsoft.control.core;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gillsoft.control.service.model.LocalityType;
import com.gillsoft.control.service.model.MappedService;
import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Price;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.ValueType;
import com.gillsoft.model.Vehicle;
import com.gillsoft.ms.entity.AdditionalServiceItem;
import com.gillsoft.ms.entity.AttributeValue;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.EntityType;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.Route;
import com.gillsoft.ms.entity.RoutePoint;
import com.gillsoft.ms.entity.SegmentTariff;
import com.gillsoft.ms.entity.TariffGrid;
import com.gillsoft.ms.entity.TariffGridPlaceQuota;
import com.gillsoft.ms.entity.Trip;
import com.gillsoft.ms.entity.User;
import com.gillsoft.util.StringUtil;
import com.google.common.base.Objects;

public class DataConverter {
	
	private static Logger LOGGER = LogManager.getLogger(DataConverter.class);
	
	public static com.gillsoft.model.Commission convert(Commission commission) {
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
	
	public static com.gillsoft.model.ReturnCondition convert(ReturnCondition returnCondition) {
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
	
	public static com.gillsoft.model.AdditionalServiceItem convert(AdditionalServiceItem additionalService) {
		com.gillsoft.model.AdditionalServiceItem converted = new com.gillsoft.model.AdditionalServiceItem();
		converted.setId(String.valueOf(additionalService.getId()));
		converted.setCode(additionalService.getCode());
		converted.setName(getValue("name", additionalService));
		for (Lang lang : Lang.values()) {
			converted.setName(lang, getValue("name_" + lang.name(), additionalService));
		}
		converted.setDescription(getValue("description", additionalService));
		for (Lang lang : Lang.values()) {
			converted.setDescription(lang, getValue("description_" + lang.name(), additionalService));
		}
		Tariff tariff = new Tariff();
		tariff.setValue(additionalService.getValue());
		Price price = new Price(Currency.valueOf(additionalService.getCurrency().name()), additionalService.getValue(), null);
		price.setTariff(tariff);
		converted.setPrice(price);
		return converted;
	}
	
	public static com.gillsoft.model.User convert(User user) {
		com.gillsoft.model.User converted = new com.gillsoft.model.User();
		converted.setId(String.valueOf(user.getId()));
		converted.setName(getValue("name", user));
		converted.setEmail(getValue("email", user));
		converted.setPhone(getValue("phone", user));
		converted.setSurname(getValue("surname", user));
		converted.setPatronymic(getValue("patronymic", user));
		return converted;
	}
	
	public static com.gillsoft.model.Organisation convert(Organisation organisation) {
		Set<String> keys = new HashSet<>();
		com.gillsoft.model.Organisation converted = new com.gillsoft.model.Organisation();
		converted.setAddress(getValue("address", organisation, keys));
		for (Lang lang : Lang.values()) {
			converted.setAddress(lang, getValue("address_" + lang.name(), organisation, keys));
		}
		converted.setName(getValue("name", organisation, keys));
		for (Lang lang : Lang.values()) {
			converted.setName(lang, getValue("name_" + lang.name(), organisation, keys));
		}
		
		String emails = getValue("email", organisation, keys);
		if (emails != null) {
			converted.setEmails(Collections.singletonList(emails));
		}
		String phones = getValue("phone", organisation, keys);
		if (phones != null) {
			converted.setPhones(Collections.singletonList(phones));
		}
		converted.setTradeMark(getValue("trade_mark", organisation, keys));
		converted.setTimezone(getValue("timezone", organisation, keys));
		converted.setProperties(getOtherAttributes(organisation, keys));
		converted.setId(String.valueOf(organisation.getId()));
		return converted;
	}
	
	public static String getValue(String name, BaseEntity entity, Set<String> keys) {
		keys.add(name);
		return getValue(name, entity);
	}
	
	public static String getValue(String name, BaseEntity entity) {
		AttributeValue value = getAttributeValue(name, entity);
		if (value != null) {
			return value.getValue();
		}
		return null;
	}
	
	public static AttributeValue getAttributeValue(String name, BaseEntity entity) {
		for (AttributeValue value : entity.getAttributeValues()) {
			if (value.getAttribute() != null
					&& name.equals(value.getAttribute().getName())) {
				return value;
			}
		}
		return null;
	}
	
	private static ConcurrentMap<String, String> getOtherAttributes(BaseEntity entity, Set<String> keys) {
		ConcurrentMap<String, String> props = null;
		for (AttributeValue value : entity.getAttributeValues()) {
			if (value.getAttribute() != null
					&& !keys.contains(value.getAttribute().getName())) {
				if (props == null) {
					props = new ConcurrentHashMap<>();
				}
				props.put(value.getAttribute().getName(), value.getValue());
			}
		}
		return props;
	}
	
	/*
	 * Создает транспорт по данным мапинга.
	 */
	public static Vehicle createVehicle(Mapping mapping, Lang lang, Vehicle original) {
		if (mapping.getAttributes() != null
				|| mapping.getLangAttributes() != null) {
			Vehicle vehicle = new Vehicle();
			vehicle.setId(String.valueOf(mapping.getId()));
			if (lang == null
					|| mapping.getLangAttributes() != null) {
				for (Entry<Lang, ConcurrentMap<String, String>> entry : mapping.getLangAttributes().entrySet()) {
					if (entry.getValue().containsKey("MODEL")) {
						vehicle.setModel(entry.getValue().get("MODEL"));
						break;
					}
				}
			} else if (mapping.getAttributes() != null) {
				vehicle.setModel(mapping.getAttributes().get("MODEL"));
			}
			if (vehicle.getModel() == null) {
				vehicle.setModel(original.getModel());
			}
			if (mapping.getAttributes() != null) {
				if (mapping.getAttributes().containsKey("CAPACITY")) {
					try {
						vehicle.setCapacity(Integer.valueOf(mapping.getAttributes().get("CAPACITY")));
					} catch (NumberFormatException e) {
						LOGGER.error("Invalid capacity for busmodel id: " + mapping.getId() + " " + e.getMessage());
					}
				} else {
					vehicle.setCapacity(original.getCapacity());
				}
				vehicle.setNumber(mapping.getAttributes().containsKey("NUMBER") ?
						mapping.getAttributes().get("NUMBER") : original.getModel());
			}
			return vehicle;
		} else {
			original.setId(String.valueOf(mapping.getId()));
			return original;
		}
	}
	
	/*
	 * Создает организацию по данным мапинга.
	 */
	public static com.gillsoft.model.Organisation createOrganisation(Mapping mapping, Lang lang, com.gillsoft.model.Organisation original) {
		if (mapping.getAttributes() != null
				|| mapping.getLangAttributes() != null) {
			com.gillsoft.model.Organisation organisation = new com.gillsoft.model.Organisation();
			organisation.setId(String.valueOf(mapping.getId()));
			if (lang == null
					&& mapping.getLangAttributes() != null) {
				for (Entry<Lang, ConcurrentMap<String, String>> entry : mapping.getLangAttributes().entrySet()) {
					organisation.setName(entry.getKey(), entry.getValue().containsKey("NAME") ?
							entry.getValue().get("NAME") : original.getName(entry.getKey()));
					organisation.setAddress(entry.getKey(), entry.getValue().containsKey("ADDRESS") ?
							entry.getValue().get("ADDRESS") : original.getAddress(entry.getKey()));
				}
			} else if (mapping.getAttributes() != null) {
				if (mapping.getAttributes().containsKey("NAME")) {
					organisation.setName(lang, mapping.getAttributes().get("NAME"));
				} else {
					organisation.setName(original.getName());
				}
				if (mapping.getAttributes().containsKey("ADDRESS")) {
					organisation.setAddress(lang, mapping.getAttributes().get("ADDRESS"));
				} else {
					organisation.setAddress(original.getAddress());
				}
			}
			if (mapping.getAttributes() != null) {
				organisation.setTradeMark(mapping.getAttributes().containsKey("TRADEMARK") ?
						mapping.getAttributes().get("TRADEMARK") : original.getTradeMark());
				organisation.setTimezone(mapping.getAttributes().containsKey("TIMEZONE") ?
						mapping.getAttributes().get("TIMEZONE") : original.getTimezone());
				if (mapping.getAttributes().containsKey("PHONES")) {
					organisation.setPhones(Arrays.asList(mapping.getAttributes().get("PHONES").split(";")));
				} else {
					organisation.setPhones(original.getPhones());
				}
				if (mapping.getAttributes().containsKey("EMAILS")) {
					organisation.setEmails(Arrays.asList(mapping.getAttributes().get("EMAILS").split(";")));
				} else {
					organisation.setEmails(original.getEmails());
				}
				List<String> keys = Arrays.asList(new String[] { "NAME", "ADDRESS", "TRADEMARK", "PHONES", "EMAILS", "TIMEZONE" });
				ConcurrentMap<String, String> props = null;
				for (Entry<String, String> attr : mapping.getAttributes().entrySet()) {
					if (!keys.contains(attr.getKey())) {
						if (props == null) {
							props = new ConcurrentHashMap<>();
						}
						props.put(attr.getKey(), attr.getValue());
					}
				}
				organisation.setProperties(props);
			}
			return organisation;
		} else {
			original.setId(String.valueOf(mapping.getId()));
			return original;
		}
	}
	
	public static Locality createLocality(Mapping mapping, Lang lang, Locality original) {
		Locality locality = new Locality();
		locality.setId(String.valueOf(mapping.getId()));
		if (mapping.getAttributes() != null
				|| mapping.getLangAttributes() != null) {
			if (lang == null
					&& mapping.getLangAttributes() != null) {
				for (Entry<Lang, ConcurrentMap<String, String>> entry : mapping.getLangAttributes().entrySet()) {
					locality.setName(entry.getKey(), entry.getValue().containsKey("NAME") ?
							entry.getValue().get("NAME") : (original != null ? original.getName(entry.getKey()) : null));
					locality.setAddress(entry.getKey(), entry.getValue().containsKey("ADDRESS") ?
							entry.getValue().get("ADDRESS") : (original != null ? original.getAddress(entry.getKey()) : null));
				}
			} else if (mapping.getAttributes() != null) {
				if (mapping.getAttributes().containsKey("NAME")) {
					locality.setName(lang, mapping.getAttributes().get("NAME"));
				} else if (original != null) {
					locality.setName(original.getName());
				}
				if (mapping.getAttributes().containsKey("ADDRESS")) {
					locality.setAddress(lang, mapping.getAttributes().get("ADDRESS"));
				} else if (original != null) {
					locality.setAddress(original.getAddress());
				}
			}
			if (mapping.getAttributes() != null) {
				locality.setLatitude(createDecimal(mapping.getId(), mapping.getAttributes().get("LATITUDE")));
				locality.setLongitude(createDecimal(mapping.getId(), mapping.getAttributes().get("LONGITUDE")));
				locality.setTimezone(mapping.getAttributes().get("TIMEZONE"));
				locality.setDetails(mapping.getAttributes().get("DETAILS"));
				locality.setType(LocalityType.get(mapping.getAttributes().get("TYPE")).name());
			}
			return locality;
		}
		if (original != null) {
			original.setId(String.valueOf(mapping.getId()));
			return original;
		}
		return locality;
	}
	
	public static Segment createSegment(Mapping mapping, Segment original) {
		Segment segment = new Segment();
		segment.setId(String.valueOf(mapping.getId()));
		if (mapping.getAttributes() != null) {
			segment.setTripId(mapping.getAttributes().get("id"));
			return segment;
		}
		if (original != null) {
			original.setId(String.valueOf(mapping.getId()));
			return original;
		}
		return segment;
	}
	
	public static Segment createSegment(String tripId, Segment original) {
		Segment segment = new Segment();
		segment.setId(original.getId());
		segment.setTripId(tripId);
		return segment;
	}
	
	private static BigDecimal createDecimal(long mappingId, String value) {
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			LOGGER.error("Invalid latitude or longitude for geo point id: " + mappingId + " " + e.getMessage());
			return null;
		}
	}
	
	public static void addMappedTrips(Map<String, Locality> localities, Segment segment, List<Trip> fromMapping) {
		if (!fromMapping.isEmpty()) {
			try {
				if (fromMapping.size() > 1) {
					fromMapping = sort(fromMapping);
				}
				Set<MappedService> services = new HashSet<>();
				Trip first = fromMapping.get(0);
				MappedService firstService = createFirstMappedService(first, localities, segment);
				services.add(firstService);
				
				Date departure = segment.getDepartureDate();
				StringBuilder tripId = new StringBuilder();
				tripId.append(";").append(first.getId()).append("=").append(StringUtil.fullDateFormat.format(departure)).append(";");
				
				if (fromMapping.size() > 1) {
					String time = StringUtil.timeFormat.format(departure);
					int arrivalDays = addedDays(first, segment.getDeparture().getId(), localities, time);
						
					for (int i = 1; i < fromMapping.size(); i++) {
						Trip next = fromMapping.get(i);
						
						Calendar nextDate = Calendar.getInstance();
						nextDate.setTime(StringUtil.fullDateFormat.parse(
								StringUtil.dateFormat.format(departure) + " " + getDepartureTime(next)));
						nextDate.add(Calendar.DATE, arrivalDays);
						
						MappedService mappedService = null;
						if (i == fromMapping.size() - 1) {
							mappedService = createLastMappedService(next, localities, segment, nextDate.getTime());
						} else {
							mappedService = createMappedService(next, localities, nextDate.getTime());
						}
						mappedService.setOrder(i);
						services.add(mappedService);
						
						tripId.append(next.getId()).append("=").append(StringUtil.fullDateFormat.format(nextDate)).append(";");
						
						arrivalDays += getLastArrivalDay(next);
					}
				} else {
					MappedService mappedService = createLastMappedService(first, localities, segment, departure);
					firstService.setToId(mappedService.getToId());
					firstService.setToDeparture(mappedService.getToDeparture());
				}
				segment.setTripId(tripId.toString());
				addMappedServices(segment, services);
			} catch (Exception e) {
				LOGGER.error("Can not set tripId from mapping " + e.getMessage());
			}
		}
	}
	
	private static void addMappedServices(Segment segment, Set<MappedService> services) {
		if (segment.getAdditionals() == null) {
			segment.setAdditionals(new HashMap<>());
		}
		segment.getAdditionals().put(MappedService.MAPPED_SERVICES_KEY, services);
	}
	
	private static MappedService createFirstMappedService(Trip trip, Map<String, Locality> localities,
			Segment segment) throws ParseException {
		MappedService mappedService = new MappedService();
		mappedService.setOrder(0);
		mappedService.setTripId(trip.getId());
		mappedService.setCarrierId(getCarrierId(trip));
		mappedService.setFromDeparture(segment.getDepartureDate());
		
		List<RoutePoint> route = getRoute(trip);
		RoutePoint point = getRoutePoint(route, segment.getDeparture().getId(), localities,
				StringUtil.timeFormat.format(segment.getDepartureDate()));
		mappedService.setFromId(Long.parseLong(point.getLocality().getParent().getId()));
		mappedService.setToId(Long.parseLong(route.get(route.size() - 1).getLocality().getParent().getId()));
		
		Calendar tripDate = Calendar.getInstance();
		tripDate.setTime(StringUtil.fullDateFormat.parse(StringUtil.dateFormat.format(segment.getDepartureDate())
				+ " " + getDepartureTime(route)));
		tripDate.add(Calendar.DATE, -1 * point.getArrivalDay());
		mappedService.setTripDeparture(tripDate.getTime());
		
		mappedService.setToDeparture(getArrivalDate(route, tripDate.getTime()));
		
		return mappedService;
	}
	
	private static long getCarrierId(Trip trip) {
		for (BaseEntity parent : trip.getParents()) {
			if (parent.getType() == EntityType.CARRIER) {
				return parent.getId();
			}
		}
		return 0;
	}
	
	private static Date getArrivalDate(List<RoutePoint> route, Date tripDate) throws ParseException {
		Calendar arrivalDate = Calendar.getInstance();
		arrivalDate.setTime(StringUtil.fullDateFormat.parse(StringUtil.dateFormat.format(tripDate)
				+ " " + getArrivalTime(route)));
		arrivalDate.add(Calendar.DATE, route.get(route.size() - 1).getArrivalDay());
		return arrivalDate.getTime();
	}
	
	private static MappedService createMappedService(Trip trip, Map<String, Locality> localities,
			Date departure) throws ParseException {
		MappedService mappedService = new MappedService();
		mappedService.setTripId(trip.getId());
		mappedService.setCarrierId(getCarrierId(trip));
		mappedService.setTripDeparture(departure);
		mappedService.setFromDeparture(departure);
		
		List<RoutePoint> route = getRoute(trip);
		mappedService.setFromId(Long.parseLong(route.get(0).getLocality().getParent().getId()));
		mappedService.setToId(Long.parseLong(route.get(route.size() - 1).getLocality().getParent().getId()));
		mappedService.setToDeparture(getArrivalDate(route, departure));
		
		return mappedService;
	}
	
	private static MappedService createLastMappedService(Trip trip, Map<String, Locality> localities,
			Segment segment, Date departure) {
		MappedService mappedService = new MappedService();
		mappedService.setTripId(trip.getId());
		mappedService.setCarrierId(getCarrierId(trip));
		mappedService.setTripDeparture(departure);
		mappedService.setFromDeparture(departure);
		mappedService.setToDeparture(segment.getArrivalDate());
		
		List<RoutePoint> route = getRoute(trip);
		RoutePoint point = getRoutePoint(route, segment.getArrival().getId(), localities, null);
		mappedService.setFromId(Long.parseLong(route.get(0).getLocality().getParent().getId()));
		mappedService.setToId(Long.parseLong(point.getLocality().getParent().getId()));
		
		return mappedService;
	}
	
	private static List<Trip> sort(List<Trip> trips) {
		List<Trip> result = new ArrayList<>();
		trips = getUniq(trips);
		checkConnected(trips);
		result.add(trips.remove(0));
		while (!trips.isEmpty()) {
			List<RoutePoint> first = getRoute(result.get(0));
			List<RoutePoint> last = getRoute(result.get(result.size() - 1));
			for (Trip trip : trips) {
				List<RoutePoint> route = getRoute(trip);
				if (isBefore(first, route)) {
					result.add(0, trip);
					trips.remove(trip);
					break;
				}
				if (isAfter(last, route)) {
					result.add(trip);
					trips.remove(trip);
					break;
				}
			}
		}
		return result;
	}
	
	private static List<Trip> getUniq(List<Trip> trips) {
		return new ArrayList<>(trips.stream().collect(Collectors.toMap(Trip::getId, t -> t, (t1, t2) -> t1)).values());
	}
	
	private static void checkConnected(List<Trip> trips) {
		for (int i = 0; i < trips.size(); i++) {
			List<RoutePoint> route = getRoute(trips.get(i));
			boolean connected = false;
			for (int j = 0; j < trips.size(); j++) {
				List<RoutePoint> other = getRoute(trips.get(j));
				if (isBefore(other, route)
						|| isAfter(other, route)) {
					connected = true;
					break;
				}
			}
			if (!connected) {
				throw new IllegalArgumentException("Trip is not connected. Invalid tripId = " + trips.get(i).getId());
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<RoutePoint> getRoute(Trip trip) {
		List<Route> routes = (List<Route>) getChildsByType(trip, EntityType.ROUTE);
		if (!routes.isEmpty()) {
			Route route = routes.get(0);
			if (route != null
					&& route.getPoints() != null
					&& !route.getPoints().isEmpty()) {
				return route.getPoints();
			}
		}
		throw new NullPointerException("Empty route. tripId = " + trip.getId());
	}
	
	public static void setTariff(MappedService mappedService, Trip trip, Price price, long departureId, long arrivalId) {
		List<SegmentTariff> tariffs = getTariffs(trip);
		if (price != null
				&& price.getTariff() != null) {
			SegmentTariff segmentTariff = getTariff(price, tariffs, departureId, arrivalId);
			if (segmentTariff == null) {
				segmentTariff = getTariff(price, tariffs, mappedService.getFromId(), mappedService.getToId());
			}
			if (segmentTariff != null) {
				setTariff(mappedService, segmentTariff);
			}
		}
	}
	
	private static SegmentTariff getTariff(Price price, List<SegmentTariff> tariffs, long departureId, long arrivalId) {
		String tariffId = price.getTariff().getId();
		com.gillsoft.ms.entity.Currency currency = com.gillsoft.ms.entity.Currency.valueOf(price.getCurrency().name());
		for (SegmentTariff segmentTariff : tariffs) {
			if (segmentTariff.getFromId() == departureId
					&& segmentTariff.getToId() == arrivalId
					&& segmentTariff.getCurrency() == currency) {
				if (String.valueOf(segmentTariff.getTariffId()).equals(tariffId)) {
					return segmentTariff;
				} else if (segmentTariff.getTariffGridPlaceQuota() != null) {
					Optional<TariffGridPlaceQuota> optional = segmentTariff.getTariffGridPlaceQuota().stream()
							.filter(pq -> pq.getId().equals(tariffId)).findFirst();
					if (optional.isPresent()) {
						TariffGridPlaceQuota placeQuota = optional.get();
						SegmentTariff tariff = new SegmentTariff();
						tariff.setCurrency(segmentTariff.getCurrency());
						tariff.setValue(placeQuota.getPrice());
						return tariff;
					}
				}
			}
		}
		return null;
	}
	
	private static void setTariff(MappedService mappedService, SegmentTariff tariff) { 
		mappedService.setTariffValue(tariff.getValue());
		mappedService.setCurrency(Currency.valueOf(tariff.getCurrency().name()));
	}
	
	@SuppressWarnings("unchecked")
	private static List<SegmentTariff> getTariffs(Trip trip) {
		List<TariffGrid> grids = (List<TariffGrid>) getChildsByType(trip, EntityType.TARIFF_GRID);
		if (!grids.isEmpty()) {
			TariffGrid grid = grids.get(0);
			if (grid != null
					&& grid.getSegmentTariffs() != null
					&& !grid.getSegmentTariffs().isEmpty()) {
				return grid.getSegmentTariffs();
			}
		}
		throw new NullPointerException("Empty tariffs. tripId = " + trip.getId());
	}
	
	private static List<? extends BaseEntity> getChildsByType(Trip trip, EntityType type) {
		return trip.getChilds().stream().filter(e -> e.getType() == type).collect(Collectors.toList());
	}
	
	private static boolean isBefore(List<RoutePoint> curr, List<RoutePoint> route) {
		return Objects.equal(getLocalityId(curr.get(0)), getLocalityId(route.get(route.size() - 1)));
	}
	
	private static boolean isAfter(List<RoutePoint> curr, List<RoutePoint> route) {
		return Objects.equal(getLocalityId(curr.get(curr.size() - 1)), getLocalityId(route.get(0)));
	}
	
	private static String getLocalityId(RoutePoint point) {
		return point.getLocality().getParent().getId();
	}
	
	private static int addedDays(Trip trip, String fromId, Map<String, Locality> localities, String time) {
		List<RoutePoint> route = getRoute(trip);
		RoutePoint point = getRoutePoint(route, fromId, localities, time);
		return route.get(route.size() - 1).getArrivalDay() - (point != null ? point.getArrivalDay() : 0);
	}
	
	private static RoutePoint getRoutePoint(List<RoutePoint> route, String fromId, Map<String, Locality> localities, String time) {
		for (RoutePoint point : route) {
			if (isFromId(point, fromId, localities)
					&& (time == null
							|| time.equals(point.getDepartureTime())
							|| time.equals(point.getArrivalTime()))) {
				return point;
			}
		}
		return null;
	}
	
	private static boolean isFromId(RoutePoint point, String fromId, Map<String, Locality> localities) {
		String stationId = point.getLocality().getId();
		String cityId = point.getLocality().getParent().getId();
		if (fromId.equals(stationId)
				|| fromId.equals(cityId)) {
			return true;
		} else {
			Locality locality = localities.get(fromId);
			if (locality != null
					&& locality.getParent() != null) {
				return isFromId(point, locality.getParent().getId(), localities);
			}
			return false;
		}
	}
	
	private static String getDepartureTime(Trip trip) {
		return getDepartureTime(getRoute(trip));
	}
	
	private static String getDepartureTime(List<RoutePoint> route) {
		return route.get(0).getDepartureTime();
	}
	
	private static String getArrivalTime(List<RoutePoint> route) {
		return route.get(route.size() - 1).getArrivalTime();
	}
	
	private static int getLastArrivalDay(Trip trip) {
		List<RoutePoint> route = getRoute(trip);
		return route.get(route.size() - 1).getArrivalDay();
	}

}
