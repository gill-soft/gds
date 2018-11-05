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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ThreadPoolStore {
	
	private static ConcurrentMap<BasePoolType, ExecutorService> executors = new ConcurrentHashMap<>();
	private static ConcurrentMap<BasePoolType, ScheduledExecutorService> scheduledExecutors = new ConcurrentHashMap<>();
	
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
	public static <T> Future<T> execute(BasePoolType poolType, Callable<T> callable) {
		return getExcecutor(poolType).submit(callable);
	}
	
	/**
	 * Добавляет задание в очередь на выполнение.
	 * 
	 * @param poolType
	 *            Пул выполнения заданий.
	 * @param runnable
	 *            Задание.
	 */
	public static void execute(BasePoolType poolType, Runnable runnable) {
		getExcecutor(poolType).submit(runnable);
	}
	
	/**
	 * Запускает задание с указанной задержкой и возвращает ссылку на него. По
	 * ссылке будет доступен результат по окончанию выполнения задания.
	 * 
	 * @param poolType
	 *            Пул выполнения заданий.
	 * @param callable
	 *            Задание.
	 * @param delay
	 *            Задержка в секундах.
	 * @return Ссылка на результат.
	 */
	public static <T> Future<T> schedule(BasePoolType poolType, Callable<T> callable, long delay) {
		return getScheduledExcecutor(poolType).schedule(callable, delay, TimeUnit.SECONDS);
	}
	
	/**
	 * Добавляет задание в очередь на выполнение с указанной задержкой.
	 * 
	 * @param poolType
	 *            Пул выполнения заданий.
	 * @param runnable
	 *            Задание.
	 * @param delay
	 *            Задержка в секундах.
	 * @return Ссылка на результат.
	 */
	public static void schedule(BasePoolType poolType, Runnable runnable, long delay) {
		getScheduledExcecutor(poolType).schedule(runnable, delay, TimeUnit.SECONDS);
	}
	
	private static ExecutorService getExcecutor(BasePoolType poolType) {
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
		return executor;
	}
	
	private static ScheduledExecutorService getScheduledExcecutor(BasePoolType poolType) {
		ScheduledExecutorService executor = scheduledExecutors.get(poolType);
		if (executor == null) {
			synchronized (poolType) {
				executor = scheduledExecutors.get(poolType);
				if (executor == null) {
					ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(poolType.name() + "-%d").build();
					executor = Executors.newScheduledThreadPool(poolType.getSize(), factory);
					scheduledExecutors.put(poolType, executor);
				}
			}
		}
		return executor;
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
	public static <T> List<Future<T>> executeAll(BasePoolType poolType, List<Callable<T>> callables) {
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
	public static <T> List<T> getResult(BasePoolType poolType, List<Callable<T>> callables) {
		List<Future<T>> futures = new ArrayList<>();
		for (Callable<T> callable : callables) {
			futures.add(execute(poolType, callable));
		}
		return getResult(futures);
	}
	
	/**
	 * Дожидается и возвращает результат функций.
	 * 
	 * @param futures
	 *            Функции
	 * @return Результат
	 */
	public static <T> List<T> getResult(List<Future<T>> futures) {
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
	
	/**
	 * Закрывает все ExecutorService
	 */
	public static void shutdown() {
		for (ExecutorService service : executors.values()) {
			service.shutdown();
		}
		for (ScheduledExecutorService service : scheduledExecutors.values()) {
			service.shutdown();
		}
	}

}
