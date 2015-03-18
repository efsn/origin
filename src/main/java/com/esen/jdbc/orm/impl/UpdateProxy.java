package com.esen.jdbc.orm.impl;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Update;
import com.esen.jdbc.orm.cache.ORMCacheManager;
import com.esen.util.exp.Expression;

/**
 * UpdateExecuter 代理类
 * 
 * @author liujin
 *
 * @param <T>
 */
public class UpdateProxy<T> implements Update<T> {
	/**
	 * UpdateExecuter 对象
	 */
	private UpdateExecuter<T> updateExe = null;

	/**
	 * 构造方法
	 * 
	 * @param updateExe UpdateExecuter 对象
	 */
	public UpdateProxy(UpdateExecuter updateExe) {
		this.updateExe = updateExe;
	}

	/**
	 * {@inheritDoc}
	 */
	public Update setCondition(Expression conditionExp, Object... params) {
		return this.updateExe.setCondition(conditionExp, params);
	}

	/**
	 * {@inheritDoc}
	 */
	public Update setPropertyValue(String property, Object value) {
		return this.updateExe.setPropertyValue(property, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public Update setPropertyExp(String property, Expression exp) {
		return this.updateExe.setPropertyExp(property, exp);
	}

	/**
	 * {@inheritDoc}
	 */
	public int executeUpdate() {
		EntityInfo entityInfo = this.updateExe.getEntityInfo();
		String dsname = this.updateExe.getConnName();
		ORMCacheManager.getInstance().clearCache(dsname, entityInfo.getEntityName());
		
		return this.updateExe.executeUpdate();
	}
}
