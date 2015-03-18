package com.esen.jdbc.dialect.impl;

import java.sql.SQLException;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.ProcedureMetaData;
import com.esen.jdbc.dialect.SynonymMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.TriggerMetaData;
import com.esen.util.ExceptionHandler;
import com.esen.util.StrFunc;
import com.esen.util.cache.CacheManager;
import com.esen.util.cache.CachedHashMap;

/**
 * 有缓存的DbMetaData实现类。
 * 
 * 在BI中计算分析表时可能会用到主题表对应的数据库的结构信息，包括
 * 字段列表、字段类型、字段的某些采样值等，在bi2.2时这些都是实现在主题集的“映射方案”对象中的
 * 其实这些功能应该是属于jdbc的，这样更便于利用内存，也更便于集群间同步这些信息，此类就是为
 * 这个目的而创建的
 * 
 * @author dw
 *
 */
public class DbMetaDataCacheImpl implements DbMetaData {

	private CachedHashMap mapCache;

	private ConnectionFactory conf;

	public DbMetaDataCacheImpl(ConnectionFactory conf) {
		this.conf = conf;
	}

	/**
	 * 获得一个没有缓存的数据库结构实例；
	 * @return
	 */
	DbMetaData createDbMetaData() {
		try {
			return conf.getDialect().createDbMetaData();
		}
		catch (Exception e) {
			ExceptionHandler.rethrowRuntimeException(e);
			return null;
		}
	}

	public ProcedureMetaData[] getProcedureMetaData() throws SQLException {
		return createDbMetaData().getProcedureMetaData();
	}

	public SynonymMetaData[] getSynonymMetaData() throws SQLException {
		return createDbMetaData().getSynonymMetaData();
	}

	/**
	 * 此类的缓存的主要内容就是表结构信息，在连续的访问一个表结构时，会返回缓存中的表结构实例；
	 */
	public TableMetaData getTableMetaData(String tablename)  {
		if (StrFunc.isNull(tablename)) {
			return null;
		}
		
		synchronized (this) {
			/**
			 * BI-4941 20110608
			 * 对象名，包括表名、字段名等，大多数数据库默认都是不区分大小写的；
			 * 而SybaseASE是严格区分的，如果更改排序规则，则会影响字段值是否区分大小写；
			 * 所以这里对读取表结构的缓存，做为键值的表名，使用不区分大小写的形式，
			 * 以兼容某些不区分大小写的数据库；
			 */
			String key = null;
			if(conf.getDbType().isFieldCaseSensitive()){
				key = tablename.trim();
			}else{
				key = tablename.trim().toUpperCase();
			}
			
			/**
			 * 默认的元素在map中的强引用时间设置为30秒，且设置timeout后的行为是设置为软引用，
			 * 这样，如果30秒内没有人使用元素且内存不足时，元素就可以被gc了
			 */
			if (mapCache == null) {
				this.mapCache = CacheManager.getMapCache("jdbcMetaCache");
			}
			TableMetaData tm = (TableMetaData) mapCache.get(key);
			if (tm == null) {
				tm = new TableMetaDataCacheImpl(this, key);
				mapCache.put(key, tm);
			}
			return tm;
		}
	}

	public List getTableNames() throws SQLException {
		return createDbMetaData().getTableNames();
	}

	public TriggerMetaData[] getTriggerMetaData() throws SQLException {
		return createDbMetaData().getTriggerMetaData();
	}

	public List getViewNames() throws SQLException {
		return createDbMetaData().getViewNames();
	}

	public synchronized void reset() {
		if (mapCache != null && !mapCache.isEmpty()) {
			mapCache.clear();
		}
	}

}
