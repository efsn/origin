package com.esen.jdbc.dialect;

/**
 * 说明:
 *   数据库中的触发器
 * @author zhuchx
 *
 */
public interface TriggerMetaData {

	/**
	 * 获得触发器的名称
	 * @return
	 */
	public String getName();

	/**
	 * 获得触发器作用的Table
	 * @return
	 */
	public String getAffectTable();

}
