package com.esen.jdbc.pool.impl.gbase;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

public class GBase8tPooledSQLException extends PooledSQLException {
	public GBase8tPooledSQLException(SQLException se) {
		super(se);
	}

	protected void analyseErrorCode(String sqlState, int errorCode2) {
		switch (errorCode2) {
		case -239: // Could not insert new row - duplicate value in a UNIQUE INDEX column
		case -268: // Unique constraint (root.u1179_394) violated
		{
			this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
			return;
		}
		
		case -316: // Index (...) already exists in database.
		case -310: // Table (root.t_test) already exists in database.
		{
			this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
			return;
		}
		
		case -206: // The specified table (...) is not in the database.
		{
			this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
			return;
		}
		
		case -217: // Column (...) not found in any table in the query (or SLV is undefined).
		{
			this.jdbcErrorCode = JDBC_INVALID_COLUMN;
			return;
		}
		}
		super.analyseErrorCode(sqlState, errorCode2);
	}
}
