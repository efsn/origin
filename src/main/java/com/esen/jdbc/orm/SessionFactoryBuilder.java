package com.esen.jdbc.orm;

import com.esen.jdbc.orm.impl.SessionFactoryImpl;


/**
 * 根据bean.xml和mapping对象构造SessionFactory工厂
 *
 * @author wang
 */
public class SessionFactoryBuilder {


	/**
	 * 根据实体管理器获取SessionFactory
	 * @param eneityManager 实体管理器
	 * @return SessionFactory对象
	 */
	public static SessionFactory build(String datasource, EntityInfoManager entityManager) {
		return new SessionFactoryImpl(datasource, entityManager);
	}

}
