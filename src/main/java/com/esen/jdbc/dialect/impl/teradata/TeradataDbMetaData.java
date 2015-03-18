package com.esen.jdbc.dialect.impl.teradata;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

/**
 * Teradata 数据库元信息
 * 
 * @author liujin
 */
public class TeradataDbMetaData extends DbMetaDataImpl {

	/**
	 * 构造方法
	 * @param con 数据库连接
	 */
	public TeradataDbMetaData(Connection con) {
		super(con);
	}

	/**
	 * 构造方法
	 * @param dbf 数据库连接和定义
	 */
	public TeradataDbMetaData(ConnectionFactory dbf) {
		super(dbf);
	}

	/**
	 * 创建数据库表元信息对象
	 * @param tablename 表名
	 * @return 表元信息
	 */
	protected TableMetaData createTableMetaData(String tablename) {
		return new TeradataTableMetaData(this, tablename);
	}
}
