package com.esen.jdbc.orm.cache;

import java.util.ArrayList;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import com.esen.jdbc.orm.CachePolicy;
import com.esen.jdbc.orm.ORMException;

/**
 * Orm 的缓存
 * 
 * @author liujin
 *
 */
public class ORMCache extends Cache {

	private CachePolicy cachePolicy = null;
	
	protected ORMCache(String cacheName, CachePolicy policy) {
		super(cacheName, policy.getMaxsize(),
				MemoryStoreEvictionPolicy.fromString(policy.getPolicy()), policy.isOverflowToDisk(), 
				null, false, policy.getLiveTime(), policy.getIdleTime(), false, 120L, null);
		
		this.cachePolicy = policy;
	}

	public void put(CacheKey key, Object value) {
		if (!cachePolicy.isEnable()) {
			return;
		}
		Element element = new Element(key, value);
		super.put(element);
	}

	public Object get(CacheKey key)
			throws CacheException {
		if (!cachePolicy.isEnable()) {
			return null;
		}

		Element element = super.get(key);
		if (element == null) {
			return null;
		}
		
		Object value = element.getValue();
		if (value == null) {
			return null;
		}
		
		try {
			return ((Element) element.clone()).getValue();
		} catch (CloneNotSupportedException e) {
			throw new ORMException(e);
		}
	}
	
	public void removeAll() {
		if (!cachePolicy.isEnable()) {
			return;
		}
		
		super.removeAll();
	}
}
