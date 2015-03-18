package com.esen.jdbc.orm.impl;

import java.util.List;

import com.esen.jdbc.orm.DataChangeEventListener;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.SessionFactory;

/**
 *
 * @author wang
 */
public class SessionFactoryImpl implements SessionFactory {
	
	private EntityInfoManager entityManager;
	
	/**
	 * 数据库连接池名
	 */
	//不使用  ConnectionFactory 的原因是避免连接池重启以后该对象失效
	//private ConnectionFactory connfactory;
	private String dsname;

	/**
	 * 构造方法
	 */
	public SessionFactoryImpl(String dsname, EntityInfoManager entityManager) {
		this.dsname = dsname;
		this.entityManager = entityManager;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Session openSession() {
		return new SessionImpl(dsname, entityManager);
	}

	/**
	 * {@inheritDoc}   
	 */
	public EntityInfoManager getEntityManager() {
		return entityManager;
	}

	/**
	 * {@inheritDoc}   
	 */
	public void regListener(DataChangeEventListener listener) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}   
	 */
	public List<DataChangeEventListener> listRegedListeners() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}   
	 */
	public void clearCache(String EntityName) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}   
	 */
	public void clearCache() {
		// TODO Auto-generated method stub

	}

}
