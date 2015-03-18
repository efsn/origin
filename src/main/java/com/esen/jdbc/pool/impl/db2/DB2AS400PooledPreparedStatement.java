package com.esen.jdbc.pool.impl.db2;

import java.io.Reader;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;

/**
 * DB2 AS400 有一个问题, 它不能使用setObject(parameterIndex, null)
 * 这里增加一个PreparedStatement的实现类, 用于分开处理二种不同DB2环境的问题
 */

public class DB2AS400PooledPreparedStatement extends DB2PooledStatement {

  public DB2AS400PooledPreparedStatement(PooledConnection conn,
      PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return _pstat.getParameterMetaData();
  }
  
  protected void setCharacterStream2(int parameterIndex, Reader reader,
      int length) throws SQLException {
    if ( (reader == null) || (length == 0)) {
      _pstat.setString(parameterIndex, null);
      return;
    }
    //length长度可能是很大一个值
    String str = getStrFromReader(reader);
    _pstat.setString(parameterIndex, str);
    //CharArrayReader r = new CharArrayReader(str.toCharArray());
    //_pstat.setCharacterStream(parameterIndex, r, str.length());
  }
  
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
  	sqlType = getParameterMetaData().getParameterType(parameterIndex);
  	/**
  	 * UTIL中VFS实现传递过来的参数有问题, 似乎是没有实现jdbcType
  	 * 以后应该在VFS中纠正改参数错误的原因, 先临时修改摒弃这个参数, 从metadata中获取
  	 */
  	if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug("setNull: " + parameterIndex + "  " + sqlType);
    _pstat.setNull(parameterIndex, sqlType);
  }
  
  /**
   * 放弃使用setObject方法和直接传递参数为空给setString方法这二种调用方法
   * 目前看来可以最大程度的避免驱动报错
   */
  public void setString(int parameterIndex, String x) throws SQLException {
  	if(x == null || x.length() == 0) {
  		int tp = getParameterMetaData().getParameterType(parameterIndex);
  		if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
  	      pconn.logDebug("setNull: " + parameterIndex + "  " + tp);
  		_pstat.setNull(parameterIndex, tp);
  	} else {
  		super.setString(parameterIndex, x);
  	}
  }
  
}
