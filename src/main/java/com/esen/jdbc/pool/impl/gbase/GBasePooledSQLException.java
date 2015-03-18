package com.esen.jdbc.pool.impl.gbase;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

public class GBasePooledSQLException extends PooledSQLException {
	public GBasePooledSQLException(SQLException se) {
		super(se);
	}

	protected void analyseErrorCode(String sqlState, int errorCode2) {
		switch (errorCode2) {
			case 1062: {
				this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
				return;
			}
			case 1031:
			case 1050:
			case 1061: {
				this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
				return;
			}
			case 1146:
			case 1051: {
				this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
				return;
			}
			case 1054: {
				this.jdbcErrorCode = JDBC_INVALID_COLUMN;
				return;
			}
		}
		super.analyseErrorCode(sqlState, errorCode2);
	}
}
