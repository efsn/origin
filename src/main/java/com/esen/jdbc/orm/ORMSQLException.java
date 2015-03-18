package com.esen.jdbc.orm;

import java.sql.SQLException;

import com.esen.jdbc.pool.PooledSQLException;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * ORM模块里面SQL相关的异常，该异常包装了SQLException
 *
 * @author wang
 */
public class ORMSQLException extends ORMException {

	private int vendorCode;

	private String sqlState;
	
	private String sqlExceptionStr;

	/**
	 * @serial create by wang 
	 */
	private static final long serialVersionUID = 5781237999904489323L;

	public ORMSQLException(String key,String defautValue) {
		super(key,defautValue);
	}
	
	public ORMSQLException(String key,String defautValue,Object[] params) {
		super(key,defautValue,params);
	}
	
	public ORMSQLException(String key , String defaultValue,Object[] params, SQLException sqlException) {
		super(key,defaultValue,params);
		this.sqlExceptionStr = getSqlExceptionMessage(sqlException);
		this.vendorCode = sqlException.getErrorCode();
		this.sqlState = sqlException.getSQLState();
	}

	public ORMSQLException(String key , String defaultValue, SQLException sqlException) {
		this(key,defaultValue,null,sqlException);
	}	
	
	public ORMSQLException(SQLException sqlException) {
		this(null,null, sqlException);
	}
	
	private static String getSqlExceptionMessage(SQLException ex) {
		StringBuilder str = new StringBuilder();
		if (ex instanceof PooledSQLException) {
			str.append(((PooledSQLException) ex).getErrorCode2Info());
			str.append("\r\n" + ex.getMessage());
		} else {
			str.append(ex.getMessage());
			SQLException next = ex.getNextException();
			if (next != null){
				str.append("\r\nNextException:\r\n" + next.getMessage());
			}
		}
		return str.toString();
	}

	/**
	 * Retrieves the vendor-specific exception code
	 * for this <code>SQLException</code> object.
	 *
	 * @return the vendor's error code
	 */
	public int getErrorCode() {
		return vendorCode;
	}

	/**
	 * Retrieves the SQLState for this <code>SQLException</code> object.
	 *
	 * @return the SQLState value
	 */
	public String getSQLState() {
		return sqlState;
	}
	
	@Override
	public String getLocalizedMessage() {
		String msg = StrFunc.isNull(messageKey)?null:I18N.getString(messageKey, defaultValue, params);
				
		return getExceptionMessage(msg);
	}

	@Override
	public String getMessage() {
		String msg = StrFunc.isNull(messageKey)?null:I18N.getString(messageKey, defaultValue,I18N.getDefaultLocale(), params);
		return getExceptionMessage(msg);
	}
	
	private String getExceptionMessage(String msg){
		if(StrFunc.isNull(sqlExceptionStr)){
			return msg;
		}else{
			return (StrFunc.isNull(msg)?"":msg + "\r\n") + sqlExceptionStr;
		}		
	}

}
