package com.gillsoft.abstract_rest_service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.concurrent.PoolType;
import com.gillsoft.concurrent.ThreadPoolStore;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Segment;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.util.CacheUtil;

public abstract class SimpleAbstractTripSearchService<T> extends AbstractTripSearchService {

	/**
	 * 
	 * @param cache
	 * @param request
	 * @return
	 */
	public TripSearchResponse simpleInitSearchResponse(CacheHandler cache, TripSearchRequest request) {
		
		// формируем задания поиска
		List<Callable<T>> callables = new ArrayList<>();
		for (final Date date : request.getDates()) {
			if (request.getBackDates() != null
					&& !request.getBackDates().isEmpty()) {
				for (final Date backDate : request.getBackDates()) {
					for (final String[] pair : request.getLocalityPairs()) {
						addInitSearchCallables(callables, pair, date, backDate);
					}
				}
			} else {
				for (final String[] pair : request.getLocalityPairs()) {
					addInitSearchCallables(callables, pair, date);
				}
			}
		}
		// запускаем задания и полученные ссылки кладем в кэш
		return CacheUtil.putToCache(cache, ThreadPoolStore.executeAll(PoolType.SEARCH, callables));
	}
	
	/**
	 * 
	 * @param callables
	 * @param pair
	 * @param date
	 */
	public void addInitSearchCallables(List<Callable<T>> callables, String[] pair, Date date) {
		
	}
	
	/**
	 * 
	 * @param callables
	 * @param pair
	 * @param date
	 * @param backDate
	 */
	public void addInitSearchCallables(List<Callable<T>> callables, String[] pair, Date date, Date backDate) {
		
	}
	
	public TripSearchResponse simpleGetSearchResponse(CacheHandler cache, String searchId) {
		try {
			// вытаскиваем с кэша ссылки, по которым нужно получить результат поиска
			@SuppressWarnings("unchecked")
			List<Future<T>> futures = (List<Future<T>>) CacheUtil.getFromCache(cache, searchId);
			
			// список заданий на дополучение результата, которого еще не было в кэше
			List<Callable<T>> callables = new ArrayList<>();
			
			// список ссылок, по которым нет еще результата
			List<Future<T>> otherFutures = new CopyOnWriteArrayList<>();
			
			// идем по ссылкам и из выполненных берем результат, а с
			// невыполненных формируем список для следующего запроса результата
			Map<String, Vehicle> vehicles = new HashMap<>();
			Map<String, Locality> localities = new HashMap<>();
			Map<String, Organisation> organisations = new HashMap<>();
			Map<String, Segment> segments = new HashMap<>();
			List<TripContainer> containers = new ArrayList<>();
			for (Future<T> future : futures) {
				if (future.isDone()) {
					try {
						addNextGetSearchCallablesAndResult(callables, vehicles, localities, organisations, segments,
								containers, future.get());
					} catch (InterruptedException | ExecutionException e) {
					}
				} else {
					otherFutures.add(future);
				}
			}
			// запускаем дополучение результата
			if (!callables.isEmpty()) {
				otherFutures.addAll(ThreadPoolStore.executeAll(PoolType.SEARCH, callables));
			}
			// оставшиеся ссылки кладем в кэш и получаем новый ид или заканчиваем поиск
			TripSearchResponse response = null;
			if (!otherFutures.isEmpty()) {
				response = CacheUtil.putToCache(cache, otherFutures);
			} else {
				response = new TripSearchResponse();
			}
			if (!vehicles.isEmpty()) {
				response.setVehicles(vehicles);
			}
			if (!localities.isEmpty()) {
				response.setLocalities(localities);
			}
			if (!organisations.isEmpty()) {
				response.setOrganisations(organisations);
			}
			if (!segments.isEmpty()) {
				response.setSegments(segments);
			}
			if (!containers.isEmpty()) {
				response.setTripContainers(containers);
			}
			return response;
		} catch (IOCacheException e) {
			return new TripSearchResponse(null, e);
		}
	}
	
	/**
	 * 
	 * @param callables
	 * @param vehicles
	 * @param localities
	 * @param organisations
	 * @param segments
	 * @param containers
	 * @param result
	 */
	public void addNextGetSearchCallablesAndResult(List<Callable<T>> callables, Map<String, Vehicle> vehicles,
			Map<String, Locality> localities, Map<String, Organisation> organisations, Map<String, Segment> segments,
			List<TripContainer> containers, T result) {
		
	}

}
