package com.esen.jdbc.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Executer;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.cache.ORMCacheManager;
import com.esen.util.exp.Expression;

/**
 * ExecuterImpl 代理类
 * 
 * @author liujin
 *
 */
public class ExecuterProxy<T> implements Executer<T> {
	/**
	 * ExecuterImpl 对象
	 */
	private ExecuterImpl executer = null;

	/**
	 * 构造方法
	 * 
	 * @param executer ExecuterImpl对象
	 */
	public ExecuterProxy(ExecuterImpl executer) {
		this.executer = executer;
	}

	/**
	 * 清除缓存
	 */
	private void clearCache() {
		EntityInfo entityInfo = this.executer.entity;
		if (entityInfo == null) {
			return;
		}
		
		ORMCacheManager.getInstance().clearCache(executer.getConnName(), entityInfo.getEntityName());
	}

	/**
	 * {@inheritDoc}
	 */
	public int delete(Expression condition, Object[] params) {
		clearCache();
		
		return this.executer.delete(condition, params);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int delete(Object idValue) {
		clearCache();
		
		return this.executer.delete(idValue);
	}

	/**
	 * {@inheritDoc}
	 */
	public void add(Object object) {
		clearCache();
		
		this.executer.add(object);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void update(Object oldId, Object object, String...properties) {		
		clearCache();
		
		this.executer.update(oldId, object, properties);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	protected PreparedStatement createPreparedStatement(String sql) throws SQLException {
		return this.executer.createPreparedStatement(sql);
	}
	
	protected void setStatementValue(PreparedStatement pstmt, int i, Property property, Object value) throws SQLException {
		this.executer.setStatementValue(pstmt, i, property, value);
	}
	
	public String getSql(String orderbyProperties) {
		return this.executer.getSql(orderbyProperties);
	}
	
	public String getSql(Expression exp) {
		return this.executer.getSql(exp);
	}
}
