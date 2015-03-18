package com.esen.jdbc.dialect.impl.vertica;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

/**
 * Vertica 数据库信息
 * 
 * @author liujin
 *
 */
public class VerticaDataBaseInfo extends AbstractDataBaseInfo {
	public VerticaDataBaseInfo(Connection conn, String defaultSchema)
			throws SQLException {
		super(conn, defaultSchema);
		this.dbtype = SqlConst.DB_TYPE_VERTICA;
		this.testSql = "select 1 from dual";
	}

//	protected String getDefaultSchema(Connection conn) {
//		return "public";
//	}
	
	/*
	 * 使用内置 函数查询 Schema
	 */
	protected String getDefaultSchema(Connection conn) {
		try {
			Statement stat = conn.createStatement();
			try {
				ResultSet rs = stat.executeQuery("SELECT CURRENT_SCHEMA()");
				if (rs.next()) {
					return rs.getString(1);
				}
				rs.close();
			} finally {
				stat.close();
			}
		} catch (SQLException se) {
			//出错时，使用数据库的默认schema
			return "public";
		}
		return "public";
	}

	protected String getNCharByteLengthSQL() {
		return "select octet_length('中') from dual";
	}

	protected String getNCharLengthSQL() {
		return "select octet_length('中') from dual";
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxKeyOfFieldsLength() {
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxIndexOfFieldsLength() {
		//vertica不支持索引
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxVarcharFieldLength() {
		return 64000000;
	}

}
