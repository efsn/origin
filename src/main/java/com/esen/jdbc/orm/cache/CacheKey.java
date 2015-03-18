package com.esen.jdbc.orm.cache;

import java.io.Serializable;

import com.esen.util.StrFunc;

/**
 * OrmCache 的 key
 * 
 * @author liujin
 *
 */
public class CacheKey implements Cloneable, Serializable {
	/**
	 * 查询语句
	 */
	private String sql = null;
	
	/**
	 * 偏移量
	 */
	private int offset = 0;
	
	/**
	 * 行数
	 */
	private int limit = 0;
	
	/**
	 * 参数集
	 */
	private Object[] params = null;
	
	/**
	 * 构造方法
	 * 
	 * @param sql 查询语句
	 * @param offset 偏移量
	 * @param limit 行数
	 * @param params 参数集
	 */
	public CacheKey(String sql, int offset, int limit, Object ... params) {
		this.sql = format(sql); 
		this.offset = offset;
		this.limit = limit;
		this.params = params;
	}
	
	/**
	 * 获取 sql
	 * @return sql
	 */
	public String getSql() {
		return sql;
	}
	
	/**
	 * 获取行数
	 * 
	 * @return 行数
	 */
	public int getLimit() {
		return limit;
	}
	
	/**
	 * 获取偏移量
	 * 
	 * @return 偏移量
	 */
	public int getOffset() {
		return offset;
	}
	
	/**
	 * 获取参数集
	 * 
	 * @return 参数集
	 */
	public Object[] getParams() {
		return params;
	}
	
	/**
	 * 获取 key
	 * 
	 * @return key
	 */
	public String getKey() {
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(sql);
		strBuilder.append(offset);
		strBuilder.append(limit);
		
		for (int i = 0; params != null && i < params.length; i++) {
			strBuilder.append(params[i]); //TODO 数据类型
		}
		
		return strBuilder.toString();
	}
	
	public int hashCode() {
		//TODO
		String str = sql + "|" + offset + "|" + limit;
		return str.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof CacheKey)) {
			return false;
		}
		
		CacheKey key2 = (CacheKey)obj;
		
		//sql不相同
		if (!StrFunc.compareStr(sql, key2.getSql())) {
			return false;
		}
		
		if (limit != key2.limit) {
			return false;
		}
		
		if (offset != key2.offset) {
			return false;
		}
		
		Object[] params2 = key2.params;
		//均无参数：相等
		if ((params == null || params.length == 0)
				&& (params2 == null || params2.length == 0)) {
			return true;
		}
		
		//一个有参数，一个无参数：不相等
		if ((params == null && params2 != null)
				|| (params != null && params2 == null)) {
			return false;
		}
		
		//参数个数不相同：不相等
		if (params.length != params2.length) {
			return false;
		}
		
		//判断所有参数是否相同
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null) {
				if (params2[i] != null) {
					return false;
				}
				continue;
			}
			
			if (!params[i].equals(params2[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 对 sql 语句进行格式化
	 * 去掉空白字符，字母全部改成小写形式
	 */
	private String format(String sql) {
		if (StrFunc.isNull(sql)) {
			return null;
		}
		
		String tmp = sql.replaceAll("\t|\r|\n| ", "");
		return tmp.toLowerCase();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public CacheKey clone() throws CloneNotSupportedException {
		CacheKey clonedCacheKey = (CacheKey)super.clone();
		
		if (this.params != null && this.params.length > 0) {
			clonedCacheKey.params = this.params.clone();
		}
		return clonedCacheKey;
	}
}
