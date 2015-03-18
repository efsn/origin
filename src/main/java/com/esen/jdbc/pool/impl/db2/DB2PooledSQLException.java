package com.esen.jdbc.pool.impl.db2;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

/**
 * DB2异常类封装，实现统一异常代码；
 * 
 *
 * @author dw
 */
public class DB2PooledSQLException extends PooledSQLException {
	public DB2PooledSQLException(SQLException se){
		super(se);
	}
	
	protected void analyseErrorCode(String sqlState, int errorCode2) {
		/**
		 * BI-4856 20110520
		 * 需要加上sqlState是否为空的判断；
		 */
		if(sqlState!=null&&sqlState.equals("42704")&&errorCode2==-204){
			this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
		}else
		if(sqlState!=null&&sqlState.equals("42710")&&errorCode2==-601){
			this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
		}else
		if(sqlState!=null&&sqlState.equals("23505")&&errorCode2==-803){
			this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
		}else
		if(sqlState!=null&&sqlState.equals("42703")&&(errorCode2==-206||errorCode2==-205)){
			this.jdbcErrorCode = JDBC_INVALID_COLUMN;
		}else{
			super.analyseErrorCode(sqlState, errorCode2);
		}
	}
}
