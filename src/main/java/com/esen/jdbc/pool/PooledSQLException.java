package com.esen.jdbc.pool;

import java.sql.SQLException;

import com.esen.util.i18n.I18N;

/**
 * 为了统一处理异常代码，执行sql后，抛出的异常都是此类的子类；
 * 每个数据库都有不同的实现，用于转换各自不同的异常代码为统一的代码；
 * 
 * 只处理集中常见的异常代码；
 * 
 * @author dw
 */
public class PooledSQLException extends SQLException {
	/**
	 * 违反唯一约定unique constraint violated
	 */
	public static final int JDBC_UNIQUE_CONSTRAINT_VIOLATED = 500001;

	/**
	 * 名称已由现有对象使用，比如创建一个已存在的表、索引、视图
	 */
	public static final int JDBC_EXISTING_OBJECT = 500002;

	/**
	 * 不存在的对象，表或视图不存在
	 */
	public static final int JDBC_NOT_EXIST_OBJECT = 500003;

	/**
	 * 无效列名 invalid column name
	 */
	public static final int JDBC_INVALID_COLUMN = 500004;

	private SQLException se;

	/**
	 * 记录转换后统一的异常代码；
	 */
	protected int jdbcErrorCode;

	public PooledSQLException(SQLException se) {
		this(null, se);
	}

	public PooledSQLException(String message, SQLException ex) {
		super(message == null ? getMessage(ex) : message + getMessage(ex), ex.getSQLState(), ex.getErrorCode());
		if (ex instanceof PooledSQLException) {
			PooledSQLException pe = (PooledSQLException) ex;
			this.se = pe.getRealSQLException();
			/**
			 * 如果异常本来就是PooledSQLException，则统一的异常代码，不需要重新分析获得；
			 */
			this.jdbcErrorCode = pe.getErrorCode2();
		}
		else {
			this.se = ex;
			analyseErrorCode(ex.getSQLState(), ex.getErrorCode());
		}
		this.setStackTrace(ex.getStackTrace());
		
	}
	
	/**
	 * 将NextException的详细信息写入异常标题，便于快速查找异常的原因;
	 * @param ex
	 * @return
	 */
	private static String getMessage(SQLException ex){
		String ms = ex.getMessage();
		SQLException next = ex.getNextException();
		if(next!=null){
			ms = ms+"\r\nNextException:\r\n"+next.getMessage();
		}
		return ms;
	}

	public PooledSQLException() {
		super();
	}

	/**
	 * 分析数据库错误代码，将其转换成能统一识别的代码；
	 * 由子类重载；
	 * @param sqlState
	 * @param errorCode
	 */
	protected void analyseErrorCode(String sqlState, int errorCode) {
		this.jdbcErrorCode = errorCode;
	}

	/**
	 * 原始异常代码
	 * 
	 */
	public int getErrorCode() {
		return se.getErrorCode();
	}
	
	/**
	 * 返回自定义统一异常代码；
	 * 原来的代码使用标准SQLException接口方法，会覆盖原有的错误代码，可能对使用原始代码的程序产生影响；
	 * @return
	 */
	public int getErrorCode2(){
		return jdbcErrorCode;
	}
	
	/**
	 * 返回自定义统一异常代码的详细信息
	 * @return
	 */
	public String getErrorCode2Info() {
		switch (jdbcErrorCode) {
		case JDBC_UNIQUE_CONSTRAINT_VIOLATED:
			return I18N.getString("com.esen.jdbc.pool.pooledsqlexception.uniqueconstraintviolated", "违反唯一约束");
			
		case JDBC_EXISTING_OBJECT:
			return I18N.getString("com.esen.jdbc.pool.pooledsqlexception.existobject", "对象已经存在");
			
		case JDBC_INVALID_COLUMN:
			return I18N.getString("com.esen.jdbc.pool.pooledsqlexception.invalidcolumn", "无效的列名");
			
		case JDBC_NOT_EXIST_OBJECT:
			return I18N.getString("com.esen.jdbc.pool.pooledsqlexception.notexistobject", "对象不存在");
		}
		
		return "";
	}
	
	/**
	 * 返回最原始的SQLException异常类；
	 * @return
	 */
	public SQLException getRealSQLException() {
		return se;
	}

	public SQLException getNextException() {
		return se.getNextException();
	}

}
