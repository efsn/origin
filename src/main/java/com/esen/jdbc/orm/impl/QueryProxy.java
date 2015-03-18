package com.esen.jdbc.orm.impl;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.cache.CacheKey;
import com.esen.jdbc.orm.cache.ORMCache;
import com.esen.jdbc.orm.cache.ORMCacheManager;
import com.esen.util.exp.Expression;

/**
 * QueryExecuter 代理类
 * 
 * @author liujin
 *
 */
public class QueryProxy<T> implements Query<T> {
	/**
	 * QueryExecuter 对象
	 */
	private QueryExecuter queryExe = null;
	
	/**
	 * ORMCache
	 */
	ORMCache ormCache = null;
	
	/**
	 * 构造方法
	 * 
	 * @param queryExe
	 */
	public QueryProxy(QueryExecuter queryExe) {
		this.queryExe = queryExe;
		initCache();
	}
	
	/**
	 * 获取 OrmCache
	 * 
	 * @return OrmCache
	 */
	private ORMCache getCache() {
		return ormCache;
	}
	
	private void initCache() {
		EntityInfo entityInfo = this.queryExe.entity;
		if (entityInfo == null) {
			return;
		}

		ormCache = ORMCacheManager.getInstance().initCache(this.queryExe.getConnName(), entityInfo.getEntityName(), entityInfo.getCachePolicy());
	}
	
	/**
	 * 获取 CacheKey
	 * 
	 * @return CacheKey
	 */
	private CacheKey getKey(String sql, Object[] params) {
		return new CacheKey(sql, -1, -1, params);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public T get(Object idValue) {
		return get(idValue, new String[]{});
	}
	
	/**
	 * {@inheritDoc}
	 */
	public T get(Object idValue, String... propertyNames) {
		ORMCache cache = getCache();
		if (cache == null) {
			return (T) this.queryExe.get(idValue, propertyNames);
		}
		
		Property pk = this.queryExe.getPrimaryKey();
		String sql = this.queryExe.toQuerySQL(propertyNames, 
						new Expression(pk.getName() + " = ?"),
						null);
		
		CacheKey key = getKey(sql, new Object[] {idValue});
		Object obj = cache.get(key);
		if (obj != null) {
			return (T) obj;
		}
		
		obj = this.queryExe.get(idValue, propertyNames);
		cache.put(key, obj);
		return (T) cache.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryResult<T> query(Expression condition, String[] propertyNames,
			Object... params) {
		return this.queryExe.query(condition, propertyNames, params);
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryResult<T> query(Expression condition, String orderbyProperties,
			String[] propertyNames, Object... params) {
		return this.queryExe.query(condition, orderbyProperties, propertyNames, params);
	}

	/**
	 * {@inheritDoc}
	 */
	public QueryResult<T> list() {
		return this.queryExe.list();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exist(Object idValue) {
		ORMCache cache = getCache();
		if (cache == null) {
			return this.queryExe.exist(idValue);
		}
		
		String sql = this.queryExe.toExistsSQL();
		
		CacheKey key = getKey(sql, new Object[] {idValue});
		Object obj = cache.get(key);
		if (obj != null) {
			return (Boolean) obj;
		}
		
		Boolean result = this.queryExe.exist(idValue);
		cache.put(key, result);
		return (Boolean) cache.get(key);
	}
}
