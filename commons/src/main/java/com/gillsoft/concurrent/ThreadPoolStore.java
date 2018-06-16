package com.gillsoft.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ThreadPoolStore {
	
	private static ConcurrentMap<PoolType, ExecutorService> executors = new ConcurrentHashMap<>();
	
	/**
	 * Запускает задание и возвращает ссылку на него. По ссылке будет доступен
	 * результат по окончанию выполнения задания.
	 * 
	 * @param poolType
	 *            Пул выполнения заданий.
	 * @param callable
	 *            Задание.
	 * @return Ссылка на результат.
	 */
	public static <T> Future<T> execute(PoolType poolType, Callable<T> callable) {
		ExecutorService executor = executors.get(poolType);
		if (executor == null) {
			synchronized (poolType) {
				executor = executors.get(poolType);
				if (executor == null) {
					ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(poolType.name() + "-%d").build();
					executor = Executors.newFixedThreadPool(poolType.getSize(), factory);
					executors.put(poolType, executor);
				}
			}
		}
		return executor.submit(callable);
	}
	
	/**
	 * Запускает задания и возвращает список ссылок на них. По ссылке будет
	 * доступен результат по окончанию выполнения задания.
	 * 
	 * @param poolType
	 *            Пул выполнения заданий.
	 * @param callables
	 *            Список заданий на выполнение.
	 * @return Список ссылок на результат.
	 */
	public static <T> List<Future<T>> executeAll(PoolType poolType, List<Callable<T>> callables) {
		List<Future<T>> futures = new CopyOnWriteArrayList<>();
		for (Callable<T> callable : callables) {
			futures.add(execute(poolType, callable));
		}
		return futures;
	}
	
	/**
	 * Запускает задания и ждет их полного выполнения. Ответ будет возвращен
	 * только после окончания всех заданий.
	 * 
	 * @param poolType
	 *            Пул выполнения заданий.
	 * @param callables
	 *            Список заданий на выполнение.
	 * @return Список ответов заданий.
	 */
	public static <T> List<T> getResult(PoolType poolType, List<Callable<T>> callables) {
		List<Future<T>> futures = new ArrayList<>();
		for (Callable<T> callable : callables) {
			futures.add(execute(poolType, callable));
		}
		List<T> resources = new ArrayList<>();
		for (Future<T> future : futures) {
			try {
				resources.add(future.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return resources;
	}
	
	public static void shutdown() {
		for (ExecutorService service : executors.values()) {
			service.shutdown();
		}
	}

}
