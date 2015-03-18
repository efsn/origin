package com.esen.jdbc.orm.helper.impl;

import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.helper.TreeEntityInfo;
import com.esen.jdbc.orm.helper.TreeObj;
import com.esen.jdbc.orm.impl.ORMUtil;
import com.esen.util.exp.Expression;
import com.esen.util.reflect.DynaBean;

/**
 * 树形
 * @param <T>
 *
 * @author wangshzh
 */
public class TreeObjImpl<T> implements TreeObj<T> {

	/**
	 * 树形对象
	 */
	private T object;

	/**
	 * 树形实体
	 */
	private TreeEntityInfo treeEntityInfo;

	/**
	 * 会话
	 */
	protected Session session;

	private DynaBean<T> bean;

	/**
	 * @param object 树形对象
	 * @param treeEntityInfo 树形实体
	 * @param session 会话
	 */
	public TreeObjImpl(T object, TreeEntityInfo treeEntityInfo, Session session) {
		this.object = object;
		this.treeEntityInfo = treeEntityInfo;
		this.session = session;
		this.bean = DynaBean.getDynaBean(object);
	}

	/**
	 * {@inheritDoc}   
	 */
	public int getChildCount() {
		QueryResult<T> queryResult;

		try {
			Query queryExecuter = session.createQuery(treeEntityInfo.getClass(), treeEntityInfo.getEntityName());
			queryResult = queryExecuter.query(new Expression(treeEntityInfo.getUpIdPropertyName() + " = ? "), null,
					ORMUtil.getPropertyValue(treeEntityInfo, bean, treeEntityInfo.getUpIdPropertyName()));
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.treeobjimpl.1","从 javabean 的 Class 对象得到 DynaBean 时出现异常", e);
		}

		return queryResult.calcTotalCount();
	}

	/**
	 * {@inheritDoc}   
	 */
	public T getObject() {
		return this.object;
	}

	/**  
	 * 获取treeEntityInfo  
	 * @return treeEntityInfo treeEntityInfo  
	 */
	public TreeEntityInfo getTreeEntityInfo() {
		return treeEntityInfo;
	}

	/**  
	 * 设置treeEntityInfo  
	 * @param treeEntityInfo treeEntityInfo  
	 */
	public void setTreeEntityInfo(TreeEntityInfo treeEntityInfo) {
		this.treeEntityInfo = treeEntityInfo;
	}

	/**  
	 * 设置object  
	 * @param object object  
	 */
	public void setObject(T object) {
		this.object = object;
	}
}
