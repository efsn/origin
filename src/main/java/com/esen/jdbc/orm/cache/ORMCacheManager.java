package com.esen.jdbc.orm.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.CacheManager;

import com.esen.jdbc.orm.CachePolicy;
import com.esen.util.StrFunc;

/**
 * Orm 缓存管理器
 * 
 * @author liujin
 *
 */
public class ORMCacheManager {
	private static ORMCacheManager instance = null;

	private CacheManager ehCacheManager = null;
	
	private  List<ORMCacheListener> ormCacheListeners = Collections.synchronizedList(new ArrayList<ORMCacheListener>());
	
	private ORMCacheManager() {
	}
	
	public void addORMCacheListerner(ORMCacheListener ormCacheListener){
		ormCacheListeners.add(ormCacheListener);
	}
	
	public synchronized static ORMCacheManager getInstance() {
		if (instance == null) {
			CacheManager ehCacheManager = CacheManager.getInstance();
			instance = new ORMCacheManager();
			instance.ehCacheManager = ehCacheManager;
		}
		return instance;
	}

	/**
	 * 根据连接池、实体名信息获取缓存
	 * 
	 * @param connName 连接池的名字
	 * @param entityName 实体名

	 * @return ORMCache
	 */
	public ORMCache getCache(String connName, String entityName) {
		String cacheName = ORMCacheManager.getCacheName(connName, entityName);
		return (ORMCache) ehCacheManager.getCache(cacheName);
	}
	
	/**
	 * 根据缓存策略添加缓存
	 * 
	 * @param connName 连接池的名字
	 * @param entityName 实体名
	 * @param cachePolicy 缓存策略
	 * @return ORMCache
	 */
	public ORMCache addCache(String connName, String entityName, CachePolicy cachePolicy) {
		if (cachePolicy == null || !cachePolicy.isEnable()) {
			return null;
		}
		
		String cacheName = getCacheName(connName, entityName);
		ORMCache ormCache = new ORMCache(cacheName, cachePolicy);		
		ehCacheManager.addCache(ormCache);
		
		return ormCache;
	}
	
	/**
	 * 初始化缓存
	 * 该缓存存在时，不做处理
	 * 不存在时，添加该缓存
	 * 
	 * @param connName
	 * @param entityName
	 * @param cachePolicy
	 */
	public synchronized ORMCache initCache(String connName, String entityName, CachePolicy cachePolicy) {
		ORMCache ormCache = ORMCacheManager.getInstance().getCache(connName, entityName);
		if (ormCache == null) {
			ormCache = ORMCacheManager.getInstance().addCache(connName, entityName, cachePolicy);
		}
		
		return ormCache;
	}

	/**
	 * 清空缓存
	 * 
	 * @param connName 连接池的名字
	 * @param entityName 实体名
	 * @param cachePolicyName 缓存策略名
	 */
	public synchronized void clearCache(String connName, String entityName) {
		clearCache(connName, entityName, true);
	}
	
	/**
	 * 清空缓存
	 * @param connName 连接池的名字
	 * @param entityInfo 实体信息
	 * @param notifyORMCacheListener 是否触发@see ORMCacheListener#afterClearCache(String, EntityInfo)事件。true 触发，false 不触发。
	 */
	public synchronized void clearCache(String connName, String entityName, boolean notifyORMCacheListener) {
		ORMCache ormCache = getCache(connName, entityName);
		if (ormCache != null) {
			ormCache.removeAll();
		}
		
		if (notifyORMCacheListener) {
			for(ORMCacheListener ormCacheListener : ormCacheListeners){
				ormCacheListener.afterClearCache(connName, entityName);
			}
		}
	}
	
	/**
	 * 释放资源
	 */
	public synchronized static void releaseInstance() {
		if (instance != null) {
			CacheManager ehCacheManager2 = instance.ehCacheManager;
			if (ehCacheManager2 != null) {
				ehCacheManager2.shutdown();
			}
		}
		instance = null;
	}
	
	/**
	 * 构造缓存的名字
	 * 
	 * @param connName 连接池的名字
	 * @param entityName 实体名

	 * @return 缓存名字
	 */
	public static String getCacheName(String connName, String entityName) {
		StringBuilder str = new StringBuilder();
		
		str.append("[");
		if (!StrFunc.isNull(connName)) {
			str.append(connName);
		}
		
		str.append("][");
		if (!StrFunc.isNull(entityName)) {
			str.append(entityName);
		}

		str.append("]");
		
		return str.toString();
	}
}
