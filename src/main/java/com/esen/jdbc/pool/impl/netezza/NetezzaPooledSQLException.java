package com.esen.jdbc.pool.impl.netezza;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;

public class NetezzaPooledSQLException extends PooledSQLException {

	protected void analyseErrorCode(String sqlState, int errorCode) {

		switch (errorCode) {
			case 1100:
			{
				/**
				 * Netezza数据库返回错误代码都一样，暂使用message进行处理
				 */
				String message = this.getRealSQLException().getMessage();
				if(message.indexOf("already exists")!=-1){
					this.jdbcErrorCode = JDBC_EXISTING_OBJECT;
				}else if(message.indexOf("does not exist")!=-1||(message.indexOf("not found")!=-1&&message.indexOf("Relation")!=-1)){
					this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
				}else if(message.indexOf("not found")!=-1){
					this.jdbcErrorCode = JDBC_INVALID_COLUMN;
				}else{
					this.jdbcErrorCode = JDBC_NOT_EXIST_OBJECT;
				}
				return;
			}
		}
		super.analyseErrorCode(sqlState, errorCode);
	}

	public NetezzaPooledSQLException() {
		super();
	}

	public NetezzaPooledSQLException(SQLException se) {
		super(se);
	}

	public NetezzaPooledSQLException(String message, SQLException ex) {
		super(message, ex);
	}

}
