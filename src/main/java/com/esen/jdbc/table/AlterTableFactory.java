package com.esen.jdbc.table;

import com.esen.jdbc.ConnectionFactory;

/**
 * 创建AlterTable的工厂
 *
 * @author zhuchx
 */
public class AlterTableFactory {
	public static AlterTable createAlterTable(ConnectionFactory fct, String tableName) {
		return new AlterTableImpl(fct, tableName);
	}
}
