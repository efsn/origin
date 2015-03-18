package com.esen.jdbc.orm.impl;

import java.util.List;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Iterator;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.cache.CacheKey;
import com.esen.jdbc.orm.cache.ORMCache;
import com.esen.jdbc.orm.cache.ORMCacheManager;

/**
 * QueryResultImpl 代理类
 * 
 * @author liujin
 *
 */
public class QueryResultProxy implements QueryResult {
	/**
	 * QueryResultImpl 对象
	 */
	private QueryResultImpl queryResult = null;
	
	/**
	 * ORMCache
	 */
	private ORMCache ormCache = null;
	
	/**
	 * 构造方法
	 * 
	 * @param queryResult QueryResultImpl 对象
	 */
	public QueryResultProxy(QueryResultImpl queryResult) {
		if (queryResult == null) {
			throw new ORMException("com.esen.jdbc.orm.impl.queryresultproxy.1","参数不能为空");
		}
		this.queryResult = queryResult;
		
		initCache();
	}

	/**
	 * {@inheritDoc}
	 */
	public int calcTotalCount() {
		ORMCache cache = getCache();
		if (cache == null) {
			return this.queryResult.calcTotalCount();
		}
		
		String sql = this.queryResult.getSql();
		
		try {
			String calcSql = ((SessionImpl)(this.queryResult.queryExe.session)).getDialect().getCountString(sql);
			CacheKey key = getKey(calcSql, -1, -1);
			Object obj = cache.get(key);
			if (obj != null) {
				return (Integer) obj;
			}
			
			Integer result = this.queryResult.calcTotalCount();
			cache.put(key, result);			
			return (Integer) cache.get(key);
		} catch (Exception e) {
			throw new ORMException(e);
		}
	}

	/**
	 * 获取 Cache
	 * 
	 * @return
	 */
	private ORMCache getCache() {
		return ormCache;
	}
	
	private void initCache() {
		EntityInfo entityInfo = this.queryResult.queryExe.entity;
		if (entityInfo == null) {
			return;
		}
		
		String connName = this.queryResult.queryExe.getConnName();
		
		ormCache = ORMCacheManager.getInstance().initCache(connName, entityInfo.getEntityName(), entityInfo.getCachePolicy());
	}
	
	/**
	 * 获取 CacheKey
	 * 
	 * @return CacheKey
	 */
	private CacheKey getKey(String sql, int pageIndex, int pageSize) {
		int offset = -1;
		int limit = -1;
		if (pageIndex >= 0 && pageSize >= 0) {
			offset = pageIndex * pageSize;
			limit = pageSize;
		}

		return new CacheKey(sql, offset, limit, this.queryResult.getParams());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List list(int pageIndex, int pageSize) {
		ORMCache cache = getCache();
		if (cache == null) {
			return this.queryResult.list(pageIndex, pageSize);
		}
		
		CacheKey key = getKey(this.queryResult.getSql(), pageIndex, pageSize);
		Object obj = cache.get(key);
		if (obj != null) {
			return (List) obj;
		}
		
		cache.put(key, this.queryResult.list(pageIndex, pageSize));		
		return (List) cache.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator iterator(int pageIndex, int pageSize) {
		//如果使用缓存，可能会有的数据存在，有的不存在，反而影响效率。
		return this.queryResult.iterator(pageIndex, pageSize);
	}
}
