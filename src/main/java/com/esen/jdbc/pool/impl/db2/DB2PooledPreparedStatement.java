package com.esen.jdbc.pool.impl.db2;

import java.io.Reader;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;

public class DB2PooledPreparedStatement extends DB2PooledStatement {

  public DB2PooledPreparedStatement(PooledConnection conn,
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
  
  /**
   * 20091229
   * 在db2中不能直接调用PreparedStatement.setNull(parameterIndex,Types.NULL),否则会出现异常com.ibm.db2.jcc.b.SqlException: Jdbc type 0 not yet supported.
   * 此处重载父类的方法,当调用_pstat.setNull(parameterIndex,Types.NULL)时,调用_pstat.setObject(parameterIndex, null)；
   * 这里: _pstat是jdbc原始实现，使用db2 9.5 的jdbc驱动_pstat.setObject(parameterIndex, null)不出异常；
   * <pre>
   * db2测试数据库版本 8.1
1)使用db2 8的驱动，版本：2.9.31
_pstat.setNull(pindex,Types.Null); 异常com.ibm.db2.jcc.b.SqlException: Jdbc type 0 not yet supported.
_pstat.setObject(parameterIndex, null); 异常com.ibm.db2.jcc.b.SqlException: Invalid data conversion: Parameter object type is invalid for requested conversion.
        at com.ibm.db2.jcc.b.tf.setObject(tf.java:1210)
        at com.esen.jdbc.pool.impl.PooledPreparedStatement.setNull(PooledPreparedStatement.java:84)
        at com.esen.jdbc.dbdefiner.DbDefinerTest.testSetNull(DbDefinerTest.java:90)

2)使用db2 9.5.2的驱动，驱动版本：3.52.95
_pstat.setNull(pindex,Types.Null) 异常：
com.ibm.db2.jcc.b.SqlException: [jcc][10271][10295][3.50.152] 无法识别 JDBC 类型：0。 ERRORCODE=-4228, SQLSTATE=null
        at com.ibm.db2.jcc.b.wc.a(wc.java:55)
        at com.ibm.db2.jcc.b.wc.a(wc.java:102)
        at com.ibm.db2.jcc.b.cb.i(cb.java:1219)
        at com.ibm.db2.jcc.b.uk.b(uk.java:612)
        at com.ibm.db2.jcc.b.uk.setNull(uk.java:584)
        at com.esen.jdbc.pool.impl.PooledPreparedStatement.setNull(PooledPreparedStatement.java:86)
        at com.esen.jdbc.dbdefiner.DbDefinerTest.testSetNull(DbDefinerTest.java:91)

_pstat.setObject(parameterIndex, null) 无异常，正常写入；

db2测试数据库版本 9.5.2
使用两个驱动，结果同8.1版本；


解决办法：1）使用2.9.31版本驱动：封装DB2PooledPreparedStatement 调用 setNull(pindex,Types.Null) 时，执行 setObject(parameterIndex, null)，此方法在jdbc2.2中
          会先获取parameterIndex参数的数据库类型t，再调用setNull(pindex,t) ;
          2）使用3.52.95版本驱动：
             if(sqlType==Types.NULL){
               _pstat.setObject(parameterIndex, null);
             }else{
               _pstat.setNull(parameterIndex, sqlType);
             }
   * </pre>
   * 这里采用第二种办法，更换jdbc驱动，使用9.5的驱动；
   */
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
  	/**
  	 * UTIL中VFS实现传递过来的参数有问题, 似乎是没有实现jdbcType
  	 * 以后应该在VFS中纠正改参数错误的原因, 先临时修改摒弃这个参数, 从metadata中获取
  	 */
    sqlType = getParameterMetaData().getParameterType(parameterIndex);
    
  	if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug("setNull: " + parameterIndex + "  " + sqlType);
    if(sqlType==Types.NULL){
      _pstat.setObject(parameterIndex, null);
    }	else{
      _pstat.setNull(parameterIndex, sqlType);
    }
  }
}
