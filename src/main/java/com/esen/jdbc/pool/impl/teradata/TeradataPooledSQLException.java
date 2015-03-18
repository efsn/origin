package com.esen.jdbc.pool.impl.teradata;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

/**
 * 封装 Teradata 的 SQL 异常, 转换集中常见的异常代码
 *
 * @author liujin
 */
public class TeradataPooledSQLException extends PooledSQLException {

	/**
	 * 构造方法
	 */
	public TeradataPooledSQLException() {
		super();
	}

	/**
	 * 构造方法
	 * @param se 异常
	 */
	public TeradataPooledSQLException(SQLException se) {
		super(se);
	}

	/**
	 * 构造方法
	 * @param message 信息
	 * @param ex 异常
	 */
	public TeradataPooledSQLException(String message, SQLException ex) {
		super(message, ex);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void analyseErrorCode(String sqlState, int errorCode) {
		switch (errorCode) {
		case 2801: // Duplicate unique prime key error in %DBID.%TVMID.
		case 2803: // Secondary index uniqueness violation in %DBID.%TVMID.
			this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
			return;

		case 3803: // Table ’%VSTR’ already exists.
		case 3804: // View ’%VSTR’ already exists.
		case 5321: // Constraint, primary index, or secondary index with the same name "%VSTR" already exists in table.
			this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
			return;

		case 3807: // Object ’%VSTR’ does not exist.
			this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
			return;

		case 3810: // Column/Parameter ’%VSTR’ does not exist.
		case 5628: // Column %FSTR not found in %VSTR.
			this.jdbcErrorCode = JDBC_INVALID_COLUMN;
			return;

		default:
			super.analyseErrorCode(sqlState, errorCode);
			return;
		}
	}
}
