package com.esen.jdbc.dialect;

/**
 * 说明:
 *   数据库存储过程的结构
 * @author zhuchx
 *
 */
public interface ProcedureMetaData {
	/**
	 * 获得存储过程的名称
	 * @return
	 */
	public String getName();

	/**
	 * 获得存储过程是否通过了编译
	 * @return
	 */
	public boolean isValid();
}
