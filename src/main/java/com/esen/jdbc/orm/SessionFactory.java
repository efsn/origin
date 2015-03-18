package com.esen.jdbc.orm;

import java.util.List;

/**
 * QuerySession 创建工厂，一个 QuerySession 对应一个数据源(ConnectioFactory)
 *
 * @author wang
 */
public interface SessionFactory {

	/**
	 * 打开默认数据源的Session
	 * @return
	 */
	Session openSession();

	/**
	 * 获取当前SessionFactory对应的EntityManager
	 * @return EneityManager实体对象管理器
	 */
	EntityInfoManager getEntityManager();

	/**
	 * 注册一个监听器
	 * @param listener
	 */
	void regListener(DataChangeEventListener listener);
	
	/**
	 * 
	 */
	List<DataChangeEventListener> listRegedListeners();
	
	/**
	 * 清空session缓存
	 * @param EntityName
	 */
	void clearCache(String EntityName);
	
	void clearCache();
	
	

}
