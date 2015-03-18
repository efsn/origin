package com.esen.jdbc;

import com.esen.util.ObjectFactory;

/**
 * 创建ConnectFactoryManager的ObjectFactory对象.让ConnectFactoryManagerAbsImpl.createInstance()能获取到实例
 * 1. 配置objfactory.properties属性BI.JDBCMGRFACTORY=com.esen.jdbc.JdbcManagerObjFactory
 * 2. 在程序中调用JdbcManagerObjFactory.set(jdbcManager)
 * @author wang
 *
 */
public class JdbcManagerObjFactory implements ObjectFactory {

	private static ConnectFactoryManager _connMgr;

	public Object createObject() {
		return _connMgr;
	}

	public Object createObject(Object createParams) {
		return _connMgr;
	}

	public void setParam(String name, Object value) {

	}

	public static final void set(ConnectFactoryManager mgr) {
		_connMgr = mgr;
	}

}
