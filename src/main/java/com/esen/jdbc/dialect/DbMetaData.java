package com.esen.jdbc.dialect;

import java.sql.SQLException;
import java.util.List;


/**
 * 描述一个数据库的结构信息
 */
public interface DbMetaData {
	/**
	 * 获取数据库中所有表名，保存在list中返回
	 * 
	 * @return
	 */
	public List getTableNames() throws SQLException;

	/**
	 * 获取数据库中所有视图名
	 * @return
	 */
	public List getViewNames() throws SQLException;

	/**
	 * 获得某表或视图（含同义词）的结构信息
	 * @param tablename 不区分大小写的表名
	 * @return 表或视图（含同义词）存在时，返回对应的结构信息类，如果表不存在，则返回空
	 */
	public TableMetaData getTableMetaData(String tablename) ;

	/**
	 * 获得存储过程
	 * @return
	 */
	public ProcedureMetaData[] getProcedureMetaData() throws SQLException;

	/**
	 * 获得同义词
	 * @return
	 */
	public SynonymMetaData[] getSynonymMetaData() throws SQLException;

	/**
	 * 获得触发器
	 * @return
	 */
	public TriggerMetaData[] getTriggerMetaData() throws SQLException;
	
	/**
	 * 用于cache机制的实现类清空cache
	 */
	public void reset();
}
