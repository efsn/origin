package com.esen.jdbc.orm.impl;

import com.esen.jdbc.orm.DataChangeEventListener;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.SessionFactory;

/**
 * 对象更新的监听器
 *
 * @author wangshzh
 */
public class DataChangeEventListenerImpl implements DataChangeEventListener {

	/**
	 * 监听的对象
	 */
	private EntityInfo entityInfo;

	/**
	 * 通过要监听的对象构造监听器
	 * @param entityInfo 监听的对象
	 */
	public DataChangeEventListenerImpl(EntityInfo entityInfo) {
		this.entityInfo = entityInfo;
	}

	/**
	 * {@inheritDoc}   
	 */
	public void onEntityChangedEvent(SessionFactory factory, String entityName) {
		// 当前实体对象发生变化时候
		EntityInfoManager entityInfoManager = factory.getEntityManager();
		entityInfoManager.remove(entityName);
		entityInfoManager.addEntity(entityInfo);
	}

}
