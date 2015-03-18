package com.esen.jdbc.pool.impl.sybase;

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;

public class SybasePooledPreparedStatement extends SybasePooledStatement {

  public SybasePooledPreparedStatement(PooledConnection conn,
      PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  
  public SybasePooledPreparedStatement(PooledConnection conn, Statement stat) {
    super(conn,stat);
  }
  
  
  protected void mySetClob(int parameterIndex, Object x) throws SQLException{
    if(x instanceof Reader){
      Reader r = (Reader)x;
      setCharacterStream(parameterIndex,r,Integer.MAX_VALUE);
      return;
    }else if(x instanceof String){
      String str = (String)x;
      /**
       * 20090806
       * 在SybaseAse12.5, 如果定义的字符串长度太大，
       * 比如这里的varchar(1024), 使用sql查询的meta.getColumnType()获得的类型却是clob类型；
       * 这里调用setCharacterStream()写入，会报：
       * com.sybase.jdbc3.jdbc.SybSQLException: Implicit conversion from datatype 'TEXT' to 'VARCHAR' is not allowed.  Use the CONVERT function to run this query.
       * 对于clob类型，所有数据库都可以使用setString()
       * 这里改为使用setString()写入；
       * 
       * 连接SybaseAse 时driverClassName最好使用：
       * com.sybase.jdbc3.jdbc.SybDriver
       * 而不是使用：com.sybase.jdbc2.jdbc.SybDriver
       * 原因是：com.sybase.jdbc2.jdbc.SybDriver会导致写入varchar类型最大长度是256，如果超过会截取，却不报错；
       * com.sybase.jdbc3.jdbc.SybDriver没有这个问题；
       * 
       */
      setString(parameterIndex,str);
      /*CharArrayReader r = new CharArrayReader(str.toCharArray());
      setCharacterStream(parameterIndex,r,str.length());*/
      return;
    }else
    if(x instanceof Clob){
      Clob clob = (Clob)x;
      Reader r = clob.getCharacterStream();
      if(r!=null){
        setCharacterStream(parameterIndex,r,Integer.MAX_VALUE);
        return;
      }else {
        setClob(parameterIndex, clob);
        return;
      }
    }
    if(x instanceof Integer||
        x instanceof Long ||
        x instanceof Float ||
        x instanceof Double ||
        x instanceof Date ||
        x instanceof java.util.Date ||
        x instanceof Time ||
        x instanceof Timestamp){
      setString(parameterIndex,x.toString());
      return;
    }
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
    _pstat.setObject(parameterIndex, x);
  }
  
  /**
   * Sybase存空串''，读取出来确实' ',有个空格；不合理；
   * 这里将保存的空串当作null来存；
   */
  public void setString(int parameterIndex, String x) throws SQLException {
    if(x!=null&&x.length()==0){
      x = null;
    }
    super.setString(parameterIndex, x);
  }
  protected void setCharacterStream2(int parameterIndex, Reader reader,
      int length) throws SQLException {
    if ((reader == null) || (length == 0)) {
      _pstat.setString(parameterIndex, "");
      //Sybase 设置text为null，写没错，读会出错，抛出异常：
      /*
      com.sybase.jdbc2.jdbc.SybSQLException: SQL Server is now using a multi-byte char
      acter set, and the TEXT character counts have not been re-calculated using this
      character set for table 'RCZWYB_B2'.  Use DBCC FIX_TEXT on this table before run
      ning the query again.
      */
      return;
    }
    //length长度可能是很大一个值
    String str = getStrFromReader(reader);
    CharArrayReader r = new CharArrayReader(str.toCharArray());
    _pstat.setCharacterStream(parameterIndex, r, str.length());
  }
  
  protected void setBinaryStream2(int parameterIndex, InputStream x, int length)
      throws SQLException {
    if ((x == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.LONGVARBINARY);
      return;
    }
    _pstat.setBinaryStream(parameterIndex, x, length);
  }
  public ResultSet executeQuery() throws SQLException {
    ResultSet rs = new SybasePooledResultSet(_pstat.executeQuery(),pconn);
    return getLimitQeury(sql, rs);
  }

  /**
   * 20091229
   * _pstat.setNull(pindex,Types.Null) 异常：
   * java.sql.SQLException: JZ006: 捕获到 IO 例外:java.io.IOException: JZ0SL: 不受支持的 SQL 类型 0。
   * _pstat.setObject(parameterIndex, null) 异常：
   * java.sql.SQLException: JZ0SE: 为 setObject() 指定了无效的对象类型 (或空对象)。
   * 
   * 改为调用父类的setObject(parameterIndex,null);
   * 此方法先获取parameterIndex字段的类型t，再调用_pstat.setNull(parameterIndex,t)
   * 不过获取对应字段的类型需要执行一个亚查询sql ：select field1,feild2... from tablename where 1>2
   * 
   * BUG:ESENFACE-1044: modify by liujin 2014.06.13
   * Sybase 的 setNull 方法不支持 Types.CLOB 和 Types.BLOB
   * 
   */
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    if(sqlType==Types.NULL){
      setObject(parameterIndex,null);
    } else if (sqlType == Types.CLOB) {
    	super.setNull(parameterIndex, Types.VARCHAR);
	} else if (sqlType == Types.BLOB) {
		super.setNull(parameterIndex, Types.BINARY);
	} else {
      super.setNull(parameterIndex, sqlType);
    }
  }
  
  /*
   * BUG：ESENFACE-1037： modify by liujin 2014.06.13
   * 在使用 setFetchSize 时，Sybase 中需要使用 SybStatement 和  SybCursorResultSet，否则会出现异常
   * 所以避免使用 setFetchSize
   */
  public void setFetchSize(int rows) throws SQLException {
	;
  }

}
