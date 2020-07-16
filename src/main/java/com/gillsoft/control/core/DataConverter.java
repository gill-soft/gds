package com.gillsoft.control.core;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ValueType;
import com.gillsoft.model.Vehicle;
import com.gillsoft.ms.entity.AttributeValue;
import com.gillsoft.ms.entity.BaseEntity;
import com.gillsoft.ms.entity.Commission;
import com.gillsoft.ms.entity.EntityType;
import com.gillsoft.ms.entity.Organisation;
import com.gillsoft.ms.entity.ReturnCondition;
import com.gillsoft.ms.entity.Route;
import com.gillsoft.ms.entity.RoutePoint;
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
						LOGGER.error("Invalid capacity for busmodel id: " + mapping.getId(), e);
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
				locality.setType(mapping.getAttributes().get("TYPE"));
				locality.setSubtype(mapping.getAttributes().get("SUBTYPE"));
			}
			return locality;
		}
		if (original != null) {
			original.setId(String.valueOf(mapping.getId()));
			return original;
		}
		return locality;
	}
	
	public static Segment createSegment(Mapping mapping, Lang lang, Segment original) {
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
	
	private static BigDecimal createDecimal(long mappingId, String value) {
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value);
		} catch (NumberFormatException e) {
			LOGGER.error("Invalid latitude or longitude for geo point id: " + mappingId, e);
			return null;
		}
	}
	
	public static void setTripIdsWithDates(Segment segment, List<Trip> fromMapping) {
		if (!fromMapping.isEmpty()) {
			try {
				if (fromMapping.size() > 1) {
					fromMapping = sort(fromMapping);
				}
				Date departure = segment.getDepartureDate();
				StringBuilder tripId = new StringBuilder();
				tripId.append(";").append(fromMapping.get(0).getId()).append("=").append(StringUtil.fullDateFormat.format(departure)).append(";");
				
				if (fromMapping.size() > 1) {
					String time = StringUtil.timeFormat.format(departure);
					int arrivalDays = addedDays(fromMapping.get(0), time);
					
					for (int i = 1; i < fromMapping.size(); i++) {
						Trip next = fromMapping.get(i);
						Calendar nextDate = Calendar.getInstance();
						StringUtil.fullDateFormat.parse(StringUtil.dateFormat.format(departure)
								+ " " + getDepartureTime(next), new ParsePosition(0), nextDate);
						nextDate.add(Calendar.DATE, arrivalDays);
						
						tripId.append(next.getId()).append("=").append(StringUtil.fullDateFormat.format(nextDate)).append(";");
						
						arrivalDays += getLastArrivalDay(next);
					}
				}
				segment.setTripId(tripId.toString());
			} catch (Exception e) {
			}
		}
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
		return new ArrayList<>(trips.stream().collect(Collectors.toMap(Trip::getId, t -> t)).values());
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
	
	private static List<RoutePoint> getRoute(Trip trip) {
		Optional<BaseEntity> o = trip.getChilds().stream().filter(c -> c.getType() == EntityType.ROUTE).findFirst();
		if (o.isPresent()) {
			Route route = (Route) o.get();
			if (route.getPoints() != null
					&& !route.getPoints().isEmpty()) {
				return route.getPoints();
			}
		}
		throw new NullPointerException("Empty route. tripId = " + trip.getId());
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
	
	private static int addedDays(Trip trip, String time) {
		List<RoutePoint> route = getRoute(trip);
		int currDays = 0;
		for (RoutePoint point : route) {
			if (time.equals(point.getDepartureTime())) {
				currDays = point.getArrivalDay();
			}
		}
		return route.get(route.size() - 1).getArrivalDay() - currDays;
	}
	
	private static String getDepartureTime(Trip trip) {
		return getRoute(trip).get(0).getDepartureTime();
	}
	
	private static int getLastArrivalDay(Trip trip) {
		List<RoutePoint> route = getRoute(trip);
		return route.get(route.size() - 1).getArrivalDay();
	}

}
