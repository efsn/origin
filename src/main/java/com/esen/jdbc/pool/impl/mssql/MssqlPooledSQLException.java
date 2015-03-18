package com.esen.jdbc.pool.impl.mssql;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

/**
 * Sql Server 异常类封装，实现统一异常代码；
 * 
 *
 * @author dw
 */
public class MssqlPooledSQLException extends PooledSQLException {
	public MssqlPooledSQLException(SQLException se){
		super(se);
	}
	protected void analyseErrorCode(String sqlState, int errorCode2) {
		if(sqlState!=null&&(sqlState.equals("S0002")||sqlState.equalsIgnoreCase("42S02"))&&errorCode2==208 //查询不存在的表、写入不存在的表；
				||sqlState.equals("S0005")&&errorCode2==3701){//删除不存在的表；
			this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
		}else
		if(sqlState!=null&&sqlState.equals("S0001")&&(errorCode2==2714||errorCode2==1913)){//2714表示创建表名重复，1913表示创建索引名重复
			this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
		}else
		if(sqlState!=null&&sqlState.equals("S0001")&&errorCode2==2601//违反唯一索引约定
				||sqlState.equals("23000")&&errorCode2==2627){//违反主键约定
			this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
		}else 
		if(sqlState!=null&&sqlState.equals("S0001")&&errorCode2==207){//不存在的字段名
			this.jdbcErrorCode = JDBC_INVALID_COLUMN;
		}else{
			super.analyseErrorCode(sqlState, errorCode2);
		}
	}
}
