package com.esen.jdbc.pool.impl.greenplum;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.esen.jdbc.pool.PooledSQLException;

public class GreenplumPooledSQLException extends PooledSQLException {

	private final static String UNDEFINED_TABLE = "42P01";
	private final static String UNDEFINED_OBJECT = "42704";
	private final static String DUPLICATE_TABLE = "42P07";
	private final static String DUPLICATE_OBJECT = "42710";
	private final static String UNIQUE_VIOLATION = "23505";
	private final static String INVALID_COLUMN_REFERENCE = "42P10";
	private final static String UNDEFINED_COLUMN = "42703";
	
	private static Map errorCodeMap = null;
	static{
		if(errorCodeMap==null){
			errorCodeMap = new HashMap();
			errorCodeMap.put(UNDEFINED_TABLE, Integer.valueOf(JDBC_NOT_EXIST_OBJECT));
			errorCodeMap.put(UNDEFINED_OBJECT, Integer.valueOf(JDBC_NOT_EXIST_OBJECT));
			errorCodeMap.put(DUPLICATE_TABLE, Integer.valueOf(JDBC_EXISTING_OBJECT));
			errorCodeMap.put(DUPLICATE_OBJECT, Integer.valueOf(JDBC_EXISTING_OBJECT));
			errorCodeMap.put(UNIQUE_VIOLATION, Integer.valueOf(JDBC_UNIQUE_CONSTRAINT_VIOLATED));
			errorCodeMap.put(INVALID_COLUMN_REFERENCE, Integer.valueOf(JDBC_INVALID_COLUMN));
			errorCodeMap.put(UNDEFINED_COLUMN, Integer.valueOf(JDBC_INVALID_COLUMN));
		}
	}
	public GreenplumPooledSQLException(SQLException ex) {
	   super(ex);
    }


	protected void analyseErrorCode(String sqlState, int errorCode) {
		if(errorCodeMap.containsKey(sqlState)){
			this.jdbcErrorCode = ((Integer)errorCodeMap.get(sqlState)).intValue();
		}
	}
}
