package com.esen.jdbc.dialect;

/**
 * 说明:
 *   数据库中的同义词定义
 * @author zhuchx
 *
 */
public interface SynonymMetaData {
	/**
	 * 获得同义词的名称
	 * @return
	 */
	public String getName();

	/**
	 * 获得同义词对应的实际数据库表的所有者
	 * @return
	 */
	public String getTableOwner();

	/**
	 * 获得同义词对应的实际数据库表表名
	 * @return
	 */
	public String getTableName();
}
