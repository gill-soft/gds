package com.gillsoft.control.core;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.logging.log4j.Logger;

import com.gillsoft.mapper.model.Mapping;
import com.gillsoft.mapper.service.MappingService;
import com.gillsoft.model.response.Response;
import com.gillsoft.util.ContextProvider;

public class Utils {

	private Utils() {
		
	}
	
	public static boolean isError(Logger logger, Response response) {
		if (response.getError() == null) {
			return false;
		}
		logger.error("Response id: " + response.getId()
				+ " Get locations from resource error: " + response.getError().getName(),
				new Exception(response.getError().getMessage()));
		return true;
	}
	
	/**
	 * Формирует и возвращает разницу времени в HH:mm с учетом таймзон.
	 */
	public static String getTimeInRoad(Date start, Date end, long fromMappingId, long toMappingId) {
		long time = getTimeInRoadInMillis(start, end, fromMappingId, toMappingId);
		if (time == 0) {
			return "";
		}
		return String.format("%02d:%02d", (time / 1000 / 60 / 60), ((time / 1000 / 60) % 60));
	}

	public static long getTimeInRoadInMillis(Date start, Date end, long fromMappingId, long toMappingId) {
		if (end == null) {
			return 0;
		}
		int fromOffset = getLocalityOffset(fromMappingId, start);
		int toOffset = getLocalityOffset(toMappingId, start);
		return getTime(end).getTimeInMillis() - getTime(start).getTimeInMillis() + (fromOffset - toOffset);
	}
	
	@SuppressWarnings("deprecation")
	private static Calendar getTime(Date date) {
		Calendar time = Calendar.getInstance();
		time.set(Calendar.YEAR, date.getYear());
		time.set(Calendar.MONTH, date.getMonth());
		time.set(Calendar.DAY_OF_MONTH, date.getDate());
		time.set(Calendar.HOUR_OF_DAY, date.getHours());
		time.set(Calendar.MINUTE, date.getMinutes());
		time.set(Calendar.SECOND, 0);
		time.set(Calendar.MILLISECOND, 0);
		return time;
	}
	
	/**
	 * Возвращает сдвиг в милисекундах для указанной географии.
	 */
	public static int getLocalityOffset(long mappingId, Date time) {
		return getLocalityOffset(mappingId, time.getTime());
	}

	/**
	 * Возвращает сдвиг в милисекундах для указанной географии.
	 */
	public static int getLocalityOffset(long mappingId, long time) {
		return getLocalityTimeZone(mappingId).getOffset(time);
	}

	/**
	 * Возвращает сдвиг в милисекундах для указанной таймзоны.
	 */
	public static int getOffset(TimeZone timeZone, long time) {
		return timeZone.getOffset(time);
	}

	/**
	 * Возвращает сдвиг в милисекундах для указанной таймзоны.
	 */
	public static int getOffset(String zoneName, long time) {
		return getOffset(getTimeZone(zoneName), time);
	}

	/**
	 * Возвращает текущее время для указанной таймзоны.
	 */
	public static long getCurrentTimeInMilis(String zoneName) {
		return getCurrentTimeInMilis(getTimeZone(zoneName));
	}

	/**
	 * Возвращает текущее время для указанной таймзоны.
	 */
	public static long getCurrentTimeInMilis(TimeZone timeZone) {
		long time = System.currentTimeMillis();
		return time + timeZone.getOffset(time) - TimeZone.getDefault().getOffset(time);
	}

	/**
	 * Возвращает таймзону географии по ИД географии.
	 */
	public static TimeZone getLocalityTimeZone(long mappingId) {
		return getTimeZone(getLocalityTimeZoneOrNull(mappingId));
	}

	/**
	 * Возвращает таймзону географии по ИД географии.
	 */
	public static String getLocalityTimeZoneOrNull(long mappingId) {
		Mapping mapping = getMapping(mappingId);
		if (mapping != null
				&& mapping.getAttributes() != null) {
			return mapping.getAttributes().get("TIMEZONE");
		}
		return null;
	}
	
	private static Mapping getMapping(long mappingId) {
		MappingService service = ContextProvider.getBean(MappingService.class);
		if (service == null) {
			return null;
		}
		return service.getMapping(mappingId);
	}

	/**
	 * Возвращает таймзону по названию. Если название пустое, то возвращается
	 * текущая таймзона.
	 */
	public static TimeZone getTimeZone(String zoneName) {
		if (zoneName != null && !zoneName.isEmpty()) {
			return TimeZone.getTimeZone(zoneName);
		} else {
			return TimeZone.getDefault();
		}
	}

}
