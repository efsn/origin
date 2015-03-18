package com.esen.jdbc.pool.impl.mysql;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

public class MysqlPooledSQLException extends PooledSQLException {
	public MysqlPooledSQLException(SQLException se){
		super(se);
	}
	
	protected void analyseErrorCode(String sqlState, int errorCode2) {
		switch(errorCode2){
			case 1062:{//Duplicate entry 'aa' for key 'IT_TEST99'
				this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
				return;
			}
			case 1050://Table 't_test' already exists
			case 1061://Duplicate key name 'IT_TEST99'
			{
				this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
				return;
			}
			case 1146://Table 'testdb.t_test' doesn't exist
			case 1051://Unknown table 't_test'
			{
				this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
				return;
			}
			case 1054:{//Unknown column 'field3' in 'field list'
				this.jdbcErrorCode = JDBC_INVALID_COLUMN;
				return;
			}
		}
		super.analyseErrorCode(sqlState, errorCode2);
	}
}
