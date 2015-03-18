package com.esen.jdbc.dialect.impl.greenplum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class GreenplumDataBaseInfo extends AbstractDataBaseInfo {

	public GreenplumDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
	    super(conn, defaultSchema);
	    this.dbtype = SqlConst.DB_TYPE_GREENPLUM;
	    this.testSql = "select 1";
    }
	
//	protected void initDefaultSchema(Connection conn, String defaultSchema) {
//		this.default_schema = "public";
//    }
	
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

	protected String getNCharLengthSQL() {
		return "select char_length('中')";
	}

	protected String getNCharByteLengthSQL() {
		return "select length('中')";
	}

	public int getMaxKeyOfFieldsLength() {
		return -1;
	}

	public int getMaxIndexOfFieldsLength() {
		return -1;
	}

	public int getMaxVarcharFieldLength() {
		return 10485760;
	}

}
