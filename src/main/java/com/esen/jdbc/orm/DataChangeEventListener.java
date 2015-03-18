package com.esen.jdbc.orm;

/**
 * 是对象更新的监听器
 *
 * @author wang
 */
public interface DataChangeEventListener {

	/**
	 * 当前实体对象发生变化时候
	 * @param entityName
	 */
	void onEntityChangedEvent(SessionFactory factory, String entityName);

}
