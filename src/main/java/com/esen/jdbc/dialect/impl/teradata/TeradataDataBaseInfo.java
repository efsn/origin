package com.esen.jdbc.dialect.impl.teradata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

/**
 * Teradata 数据库的基本信息
 *
 * @author liujin
 */
public class TeradataDataBaseInfo extends AbstractDataBaseInfo {

	/**
	 * 构造方法
	 * @param conn 数据库连接
	 * @param defaultSchema 默认schema
	 * @throws SQLException 获取  Teradata 数据库基本信息异常时抛出该异常信息
	 */
	public TeradataDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
	    super(conn, defaultSchema);
	    this.dbtype = SqlConst.DB_TYPE_TERADATA;
	    this.testSql = "select 1";
    }
	
	/*
	 * 使用内置函数查询 Schema
	 */
	protected String getDefaultSchema(Connection conn) {
		try {
			Statement stat = conn.createStatement();
			try {
				ResultSet rs = stat.executeQuery("SELECT DATABASE");
				if (rs.next()) {
					return rs.getString(1);
				}
				rs.close();
			} finally {
				stat.close();
			}
		} catch (SQLException se) {
			return null;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getNCharLengthSQL() {
		return "select char_length('中')";
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getNCharByteLengthSQL() {
		return "select length('中')";
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxKeyOfFieldsLength() {
		return 64000;
	}


	/**
	 * {@inheritDoc}
	 */
	public int getMaxIndexOfFieldsLength() {
		return 32000;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxVarcharFieldLength() {
		return 32000;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getMaxRowsInTrans() {
		/*
		 * ISSUE:BI-8594: add by liujin 2013.07.10 
		 * 一个事务中批量插入的数据行数有限制
		 */
		return 5000;
	}
}
