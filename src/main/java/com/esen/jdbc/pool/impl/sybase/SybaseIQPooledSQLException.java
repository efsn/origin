package com.esen.jdbc.pool.impl.sybase;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

public class SybaseIQPooledSQLException extends PooledSQLException {

	public SybaseIQPooledSQLException(SQLException ex) {
		super(ex);
	}
	
	protected void analyseErrorCode(String sqlState, int errorCode2) {
		if(errorCode2==2706){ //查询不存在的表、写入不存在的表、删除不存在的表；
			this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
		}else
		if(errorCode2==12006||errorCode2==1921){//12006表示创建表名重复，1921表示创建索引名重复
			this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
		}else
		if(sqlState!=null&&sqlState.equals("QGA03")){//违反唯一索引约定,违反主键约定;
			this.jdbcErrorCode = JDBC_UNIQUE_CONSTRAINT_VIOLATED;
		}else 
		if(errorCode2==207){//不存在的字段名
			this.jdbcErrorCode = JDBC_INVALID_COLUMN;
		}else{
			super.analyseErrorCode(sqlState, errorCode2);
		}
	}
}
