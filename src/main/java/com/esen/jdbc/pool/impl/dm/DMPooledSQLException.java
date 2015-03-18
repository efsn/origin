package com.esen.jdbc.pool.impl.dm;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

/**
 * 封装 dameng 的 SQL异常；
 * 转换集中常见的异常代码；
 */
public class DMPooledSQLException extends PooledSQLException {
	
	public DMPooledSQLException() {
		super();
	}

	public DMPooledSQLException(SQLException se){
		super(se);
	}
	
	public DMPooledSQLException(String message, SQLException ex) {
		super(message, ex);
	}

	protected void analyseErrorCode(String sqlState, int errorCode) {
		switch(errorCode){
			case -6602: //违反表[%s]唯一性约束
			case -6610: //违反唯一性约束(EC_RN_REAL_DUP_KEY)
			case -6611: //违反唯一性约束(EC_RN_DUP_KEY_FOR_UNIQUE_INDEX)
			case -6612: { //违反唯一性约束(EC_RN_DUP_KEY)
				this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
				return;
			}
			
			case -2140: //索引[%s]已存在
			case -2124 :{//对象已存在
				this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
				return;
			}
			case -2104: //无效的表名
			case -2105: //无效的视图名
			case -2106: { //无效的表或视图名
				this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
				return;
			}
			case -2111: {//无效列名
				this.jdbcErrorCode = JDBC_INVALID_COLUMN;
				return;
			}
		}
		super.analyseErrorCode(sqlState, errorCode);
	}
}
