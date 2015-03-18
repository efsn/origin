package com.esen.jdbc.pool.impl.oracle;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

/**
 * 封装Oacle的SQL异常；
 * 转换集中常见的异常代码；
 *
 * @author dw
 */
public class OraclePooledSQLException extends PooledSQLException {
	
	public OraclePooledSQLException(SQLException se){
		super(se);
	}
	
	protected void analyseErrorCode(String sqlState, int errorCode2) {
		switch(errorCode2){
			case 1:{//违反唯一约束条件
				this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
				return;
			}
			case 955:{//名称已由现有对象使用
				this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
				return;
			}
			case 942:{//表或视图不存在
				this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
				return;
			}
			case 904:{//无效列名
				this.jdbcErrorCode = JDBC_INVALID_COLUMN;
				return;
			}
		}
		
		super.analyseErrorCode(sqlState, errorCode2);
	}


}
