package com.esen.jdbc.dialect.impl.dm;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.db.sql.analyse.SQLAnalyse;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;

public class DM7Dialect extends DMDialect {

	public DM7Dialect(Object f) {
		super(f);
	}

	public DbDefiner createDbDefiner() {
		return new DM7Def(this);
	}

	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null) {
			dbmd = connectionFactory != null ? new DM7DbMetaData(
					connectionFactory) : new DM7DbMetaData(con);
		}
		return dbmd;
	}

	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new DM7DbMetaData(conn);
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getResultMetaData_Sql(String sql) {
		if (SqlFunc.isCallalbe(sql)) {
			return sql;
		}

		/*
		 * 不能加外层查询，否则无法获取到准确的结果集元信息
		 */
		SQLAnalyse sqla = new SQLAnalyse(sql);
		sql = sqla.getNoOrderBySQl();
		sql = sql.replaceAll("\\?", "''");

		return sql;
	}

	/*
	 * DM7 的 INSTR 函数区分大小写
	 */
	public String funcSearch(String c1, String c2) {
		StringBuffer sBuf = new StringBuffer(100).append("(INSTR(")
				.append(c2.toUpperCase()).append(',').append(c1.toUpperCase())
				.append(")-1)"); // 数据库是从1开始计算位置
		return sBuf.toString();
	}

	public String funcFind(String c1, String c2) {
		StringBuffer sBuf = new StringBuffer(100).append("(INSTR(").append(c2)
				.append(',').append(c1).append(")-1)"); // 数据库是从1开始计算位置
		return sBuf.toString();
	}

	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset < 0 || limit <= 0)
			return querySelect;

		StringBuffer sBuf = new StringBuffer().append("select * from ( ")
				.append(querySelect).append(" ) as temp_ limit ");

		if (offset > 0) {
			sBuf.append(offset).append(", ").append(limit);
		} else {
			sBuf.append(limit);
		}

		return sBuf.toString();
	}
}
