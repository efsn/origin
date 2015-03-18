package com.esen.jdbc.pool.impl.vertica;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

public class VerticaPooledSQLException extends PooledSQLException {

	/**
	 * 构造方法
	 */
	public VerticaPooledSQLException() {
		super();
	}

	/**
	 * 构造方法
	 * @param se 异常
	 */
	public VerticaPooledSQLException(SQLException se) {
		super(se);
	}

	/**
	 * 构造方法
	 * @param message 信息
	 * @param ex 异常
	 */
	public VerticaPooledSQLException(String message, SQLException ex) {
		super(message, ex);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void analyseErrorCode(String sqlState, int errorCode) {
		switch (errorCode) {
		case 2801: // TODO
			this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
			return;

		case 4213: // ROLLBACK: Object "..." already exists
			this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
			return;

		case 4876: // ERROR: Table "..." does not exist
		case 4883: // ERROR: Table "..." does not exist
		case 4566: // ERROR: Relation "..." does not exist
			this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
			return;

		case 2624: // ERROR: Column "..." does not exist
			this.jdbcErrorCode = JDBC_INVALID_COLUMN;
			return;

		default:
			super.analyseErrorCode(sqlState, errorCode);
			return;
		}
	}
}
