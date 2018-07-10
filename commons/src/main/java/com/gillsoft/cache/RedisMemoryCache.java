package com.gillsoft.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gillsoft.util.StringUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component("RedisMemoryCache")
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class RedisMemoryCache extends MemoryCacheHandler {
	
	private static final String ALL_KEYS = "RedisMemoryCache.keys";
	
	/**
	 * Признак игнорирования наличия кэша и запускать сразу таску на обновление.
	 */
	public static final String IGNORE_CACHE = "ignoreCache";
	
	private JedisPool jedisPool;
	
	public RedisMemoryCache() {
		super();
		CustomJedisPoolConfig config = getPoolConfig();
		jedisPool = new JedisPool(config, config.getHost(), config.getPort(), config.getTimeout(), config.getPassword());
	}

	private CustomJedisPoolConfig getPoolConfig() {
		Properties properties = new Properties();
		try {
			Resource resource = new ClassPathResource("redis-pool.properties");
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
		}
		// настройка пула редиса
		CustomJedisPoolConfig poolConfig = new CustomJedisPoolConfig();
		poolConfig.setMaxIdle(Integer.parseInt(
				properties.getProperty("max.idle", "100")));
		poolConfig.setMaxTotal(Integer.parseInt(
				properties.getProperty("max.total", "100")));
		poolConfig.setMinIdle(Integer.parseInt(
				properties.getProperty("min.idle", "20")));
		poolConfig.setMaxWaitMillis(Integer.parseInt(
				properties.getProperty("max.wait", "500")));
		poolConfig.setTimeBetweenEvictionRunsMillis(Integer.parseInt(
				properties.getProperty("time.between.eviction.runs", "60000")));
		poolConfig.setNumTestsPerEvictionRun(Integer.parseInt(
				properties.getProperty("num.tests.per.eviction.run", "10")));
		poolConfig.setPort(Integer.parseInt(
				properties.getProperty("port", "6379")));
		poolConfig.setTimeout(Integer.parseInt(
				properties.getProperty("connection.timeout", "5000")));
		poolConfig.setHost(properties.getProperty("host", "127.0.0.1"));
		poolConfig.setPassword(properties.getProperty("password", "foobared"));
		return poolConfig;
	}
	
	private void setex(CacheObject cacheObject) {
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.setex(cacheObject.getName(), cacheObject.getRemainingTime(),
					StringUtil.objectToBase64String(cacheObject));
			jedis.close();
		} catch (Exception e) {
		}
	}
	
	private void set(CacheObject cacheObject) {
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.set(cacheObject.getName(), StringUtil.objectToBase64String(cacheObject));
			jedis.close();
		} catch (Exception e) {
		}
	}
	
	private void add(String key) {
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.sadd(ALL_KEYS, key);
			jedis.close();
		} catch (Exception e) {
		}
	}
	
	private CacheObject get(String key) {
		try {
			Jedis jedis = jedisPool.getResource();
			String value = jedis.get(key);
			jedis.close();
			CacheObject object = (CacheObject) StringUtil.base64StringToObject(value);
			return object;
		} catch (Exception e) {
			return null;
		}
	}
	
	private Set<String> members() {
		try {
			Jedis jedis = jedisPool.getResource();
			Set<String> value = jedis.smembers(ALL_KEYS);
			jedis.close();
			return value;
		} catch (Exception e) {
			return null;
		}
	}
	
	private void rem(String key) {
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.srem(ALL_KEYS, key);
			jedis.close();
		} catch (Exception e) {
		}
	}
	
	public void close() {
		jedisPool.close();
	}
	
	@Override
	public void write(Object storedObject, Map<String, Object> params) throws IOCacheException {
		CacheObject cacheObject = createObject(storedObject, params);
		write(cacheObject);
	}
	
	private void write(CacheObject cacheObject) {
		if (cacheObject.isEternal()) {
			set(cacheObject);
		} else {
			setex(cacheObject);
		}
	}

	/**
	 * Возвращает объект с кэша. Если объекта нет, то запускает задание UPDATE_TASK
	 * для создания кэша. При этом, если задание уже запущено или еще не выполнено,
	 * то будет брошена ошибка IOCacheException.
	 */
	@Override
	public Object read(Map<String, Object> params) throws IOCacheException {
		String key = String.valueOf(params.get(OBJECT_NAME));
		
		// проверка игнорирования кэша
		CacheObject cacheObject = null;
		if (!params.containsKey(IGNORE_CACHE)) {
			cacheObject = get(key);
		}
		// если в кэше нет объекта и есть задание на добавление объекта в кэш
		if (cacheObject == null
				&& params.get(UPDATE_TASK) != null) {
			
			// синхронизация по ключу, чтобы не выполнять два раза один и тот же поиск
			synchronized (key.intern()) {
				if (!params.containsKey(IGNORE_CACHE)) {
					cacheObject = get(key);
				}
				if (cacheObject == null
						&& getKeyFromMemoryCache(params) == null) {
					
					executor.submit((Runnable) params.get(UPDATE_TASK));
					
					// метка, что таск на обновление запущен, чтобы другие потоки не запускали то же самое
					putKeyToMemoryCache(params);
					
					// создаем сэт ключей, чтобы потом по ним запускать таски обновления
					add(key);
				}
			}
		}
		if (cacheObject == null) {
			throw new IOCacheException();
		}
		// обновляем объект в кэше с пометкой readed = true
		if (!cacheObject.isReaded()) {
			cacheObject.setReaded(true);
			setex(cacheObject);
		}
		return cacheObject.getCachedObject();
	}
	
	/*
	 * Чтение из кэша оперативки
	 */
	private Object getKeyFromMemoryCache(Map<String, Object> params) throws IOCacheException {
		return super.read(params);
	}
	
	/*
	 * Запись в кэш оперативки
	 */
	private void putKeyToMemoryCache(Map<String, Object> params) throws IOCacheException {
		Map<String, Object> copy = new HashMap<>();
		copy.putAll(params);
		copy.put(TIME_TO_LIVE, 300000l);
		copy.put(UPDATE_DELAY, null);
		copy.put(UPDATE_TASK, null);
		super.write(new Object(), copy);
	}
	
	@Scheduled(initialDelay = 10000, fixedDelay = 10000)
	@Override
	public void updateCached() {
		super.updateCached();
		long curr = System.currentTimeMillis();
		Set<String> keys = members();
		if (keys != null) {
			for (String key : keys) {
				CacheObject cacheObject = get(key);
				if (cacheObject != null) {
					
					// проверяем нужно ли обновить запись
					if (cacheObject.getUpdateTask() != null
							&& cacheObject.isReaded()
							&& cacheObject.getCreated() <= curr - cacheObject.getUpdateDelay()) {
						
						// удаляем таксу с кэша и перезаписываем, чтобы не обновлять по несколько раз
						Runnable runnable = cacheObject.getUpdateTask();
						cacheObject.setUpdateTask(null);
						write(cacheObject);
						executor.submit(runnable);
					}
				} else {
					
					// удаляем неиспользуемые ключи
					Map<String, Object> params = new HashMap<>();
					params.put(OBJECT_NAME, key);
					try {
						if (getKeyFromMemoryCache(params) == null) {
							rem(key);
						}
					} catch (IOCacheException e) {
					}
				}
			}
		}
	}

}
