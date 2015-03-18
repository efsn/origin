package com.esen.jdbc.pool.impl.oracle;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;
import com.esen.util.SysFunc;

/**
 * 20090805
 * 对Oracle8，读取大字段需要特有的方法；
 * @author dw
 *
 */
public class Oracle8PooledResultSet extends OraclePooledResultSet {

  public Oracle8PooledResultSet(ResultSet rs, PooledConnection pconn) throws SQLException {
    super(rs, pconn);
  }
  
  public String getString(int columnIndex) throws SQLException {
    switch(getSQLType(columnIndex)){
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.BIT:
      case Types.REAL:
      case Types.DECIMAL:{
        String v = _rs.getString(columnIndex);
        return getNumberStr(v);
      }
      case java.sql.Types.CLOB :{
        /**
         * 20090625
         * Oracle只有定义的clob类型才需要通过流来读取；
         * 但是经过测试clob,long 类型的字段，可以直接getString()来读取；
         * 在Oracle9,Oracle10上测试通过；
         * 注：使用的是10g的jdbc驱动；
         * 对于Oracle8 需要测试，TODO
         */
        return getStrFromReader(getCharacterStream(columnIndex));
      }
    }
    return super.getString(columnIndex);
  }
  
  private StringBuffer bufferForClob;
  private char[] cc;
  
  /**
   * 20100301
   * 将结果集中的clob字段，通过getCharacterStream()获得的Reader转换成字符串；
   * 现在做优化，中间变量做缓存重复利用，不用每次读取都创建新的对象，浪费内存；
   * 此函数现在专给Oracle8读取使用，9i以后可以直接将clob读取成String;
   * @param rr
   * @return
   * @throws SQLException
   */
  protected String getStrFromReader(Reader rr) throws SQLException {
    if(rr==null)
      return null;
    if(bufferForClob==null){
      bufferForClob = new StringBuffer(128);
    }else{
      bufferForClob.setLength(0);
    }
    if(cc==null)
      cc = new char[1024];//节约内存
    int ll = 0;
    try{
      while ( (ll = rr.read(cc)) != -1) {
        if (ll > 0) {
          bufferForClob.append(cc, 0, ll);
        }
      }
    }catch(IOException ie){
      SQLException se = new SQLException(ie.getMessage());
      se.setStackTrace(ie.getStackTrace());
      throw se;
    }
    return bufferForClob.toString();
  }

  private static final String RESULTSET_ORACLE_CLASSNAME2 = "oracle.jdbc.OracleResultSetImpl";

  private static final String RESULTSET_ORACLE_CLASSNAME = "oracle.jdbc.driver.OracleResultSetImpl";

  private static final String RESULTSET_WEBPHERE_CLASSNAME = "com.ibm.ws.rsadapter.jdbc.WSJdbcResultSet";

  private static final String RESULTSET_WEBLOGIC_CLASSNAME = "weblogic.jdbc.rmi.SerialResultSet";

  private static final String RESULTSET_JBOSS_CLASSNAME = "org.jboss.resource.adapter.jdbc.WrappedResultSet";

  private static final String RESULTSET_WEBLOGIC81_CLASSNAME = "weblogic.jdbc.wrapper.ResultSet_oracle_jdbc_driver_OracleResultSetImpl";

  private Object getOraResultSet(ResultSet rs) throws Exception {
    Class cls = rs.getClass();
    String clsname = cls.getName();
    if (clsname.equals(RESULTSET_ORACLE_CLASSNAME2)) {
      return rs;
    }
    else if (clsname.equals(RESULTSET_ORACLE_CLASSNAME)) {
      return rs;
    }
    else if (clsname.equals(RESULTSET_WEBPHERE_CLASSNAME)) {
      Field f = rs.getClass().getDeclaredField("rsetImpl");
      f.setAccessible(true);
      Object r = f.get(rs);
      f.setAccessible(false);
      return r;
    }
    else if (clsname.equals(RESULTSET_WEBLOGIC_CLASSNAME)) {
      Object o = SysFunc.getDeclaredField(rs, "rmi_rs");
      o = SysFunc.getDeclaredField(o, "t2_rs");
      o = SysFunc.getDeclaredField(o, "rs");
      return o;
    }
    else if (clsname.equals(RESULTSET_WEBLOGIC81_CLASSNAME)) {
      //Object o = SysFunc.getSuperSuperClassDeclaredField(rs, "rs");
      //return (OracleResultSet) o;
      return null;
    }
    else if (clsname.equals(RESULTSET_JBOSS_CLASSNAME)) {
      Object o = SysFunc.getDeclaredField(rs, "resultSet");
      return o;
    }
    return rs;
  }
  
  public InputStream getBinaryStream(int columnIndex) throws SQLException {
    Object ors = null;
    try {
      ors = getOraResultSet(_rs);
    }
    catch (Exception e) {
      if(_pconn.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
        _pconn.logError(e, "");
    }

    if (ors == null) {
      return _rs.getBinaryStream(columnIndex);
    }
    else {
      Object blob = null;
      try {
        Class orsClass = Class.forName("oracle.jdbc.OracleResultSet");
        //Object orsObj = orsClass.cast(ors);
        Method meth = orsClass.getMethod("getBLOB", new Class[] {
          int.class });
        blob = meth.invoke(ors, new Object[] {
          new Integer(columnIndex) });
      }
      catch (Exception ex) {
        throw new SQLException(ex.getMessage());
      }
      //BLOB blob = ors.getBLOB(columnIndex);
      if (blob == null) {
        return null;
      }
      else {
        return ((Blob) blob).getBinaryStream();
      }
    }
  }
  public InputStream getBinaryStream(String columnName) throws SQLException {
    Object ors = null;
    try{
      ors = getOraResultSet(_rs);
    }catch(Exception e){
      if(_pconn.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
        _pconn.logError(e,"");
    }
    if (ors == null) {
      return _rs.getBinaryStream(columnName);
    }
    else {
      Object blob = null;
      try {
        Class orsClass = Class.forName("oracle.jdbc.OracleResultSet");
        //Object orsObj = orsClass.cast(ors);
        Method meth = orsClass.getMethod("getBLOB", new Class[] {
          String.class });
        blob = meth.invoke(ors, new Object[] { columnName });
      }
      catch (Exception ex) {
        throw new SQLException(ex.getMessage());
      }
      //BLOB blob = ors.getBLOB(columnName);
      if (blob == null) {
        return null;
      }
      else {
        return ((Blob) blob).getBinaryStream();
      }
    }
  }
  public Reader getCharacterStream(int columnIndex) throws SQLException {
    Object ors = null;
    Reader r = null;
    try{
      ors = getOraResultSet(_rs);
    }catch(Exception e){
      if(_pconn.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
        _pconn.logError(e,"");
    }
    if (ors == null) {
      r = _rs.getCharacterStream(columnIndex);
    }
    else {
      Object clob = null;
      try {
        Class orsClass = Class.forName("oracle.jdbc.OracleResultSet");
        //Object orsObj = orsClass.cast(ors);
        Method meth = orsClass.getMethod("getCLOB", new Class[] {
          int.class });
        clob = meth.invoke(ors, new Object[] { new Integer(columnIndex) });
      }
      catch (Exception ex) {
        throw new SQLException(ex.getMessage());
      }
      //CLOB clob = ors.getCLOB(columnIndex);
      if (clob == null) {
        return null;
      }
      else {
        r = ((Clob)clob).getCharacterStream();
      }
    }
    if(r==null) return null;
    if(_pconn.get_ds().needEncoding()){
      String ss = getStrFromReader(r);
      ss = _pconn.getGBKEncodingStr(ss);
      r = new CharArrayReader(ss.toCharArray());
    }
    return r;
  }
  public Reader getCharacterStream(String columnName) throws SQLException {
    Object ors = null;
    Reader r = null;
    try{
      ors = getOraResultSet(_rs);
    }catch(Exception e){
      if(_pconn.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
        _pconn.logError(e,"");
    }
    if (ors == null) {
      r = _rs.getCharacterStream(columnName);
    }
    else {
      Object clob = null;
      try {
        Class orsClass = Class.forName("oracle.jdbc.OracleResultSet");
        //Object orsObj = orsClass.cast(ors);
        Method meth = orsClass.getMethod("getCLOB", new Class[] {
          String.class });
        clob = meth.invoke(ors, new Object[] { columnName });
      }
      catch (Exception ex) {
        throw new SQLException(ex.getMessage());
      }
      //CLOB clob = ors.getCLOB(columnName);
      if (clob == null) {
        return null;
      }
      else {
        r = ((Clob)clob).getCharacterStream();
      }
    }
    if(r==null) return null;
    if(_pconn.get_ds().needEncoding()){
      String ss = getStrFromReader(r);
      ss = _pconn.getGBKEncodingStr(ss);
      r = new CharArrayReader(ss.toCharArray());
    }
    return r;
  }
}
