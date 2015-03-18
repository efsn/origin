package com.esen.jdbc.pool.impl.oracle;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;
import com.esen.util.StmFunc;
import com.esen.util.SysFunc;


/**
 * 20090805
 * Oracle8 对大字段的写入，需要调用oracle jdbc的特有方法；
 * 这里单独抽出来处理；
 * @author dw
 *
 */
public class Oracle8PooledPreparedStatement extends Oracle8PooledStatement {

  public Oracle8PooledPreparedStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  
  public ResultSet executeQuery() throws SQLException {
    return new Oracle8PooledResultSet(_pstat.executeQuery(),pconn);
  }
  
  /**
   * 20090818
   * 只有Oracle8，写入timestamp才有这个问题；
   * oracle8 没有timestamp类型；
   */
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x);
      //数据库为oracle8i时出现了以下问题
      //如果一个字段为varchar2,一个字段为timestamp，同时更新这两个字段时varchar2字段出现乱码
      //重现：重新计算一个结果表时当前的计算结果会覆盖上一次的计算结果，这时会更新数据库中的记录，更新后结果表的fxblinkid出现乱码
      //如果将timestamp的毫秒数设置为0，则该问题消失
      if(x!=null)
        x.setNanos(0);
    _pstat.setTimestamp(parameterIndex, x);
  }
  
  protected void setBinaryStream2(int parameterIndex, InputStream x,
      int length) throws SQLException {
    if ((x == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.BLOB);
      return;
    }
    MyOracleBlob myblob = new MyOracleBlob();
    myblob.setBlob(_pstat, parameterIndex, x);
  }

  protected void setCharacterStream2(int parameterIndex, Reader reader,
      int length) throws SQLException {
    if ((reader == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.CLOB);
      return;
    }
    MyOracleClob myclob = new MyOracleClob();
    myclob.setClob(_pstat, parameterIndex, reader);
  }
  
  private Object getDeclaredStaticField(Class cls, String fld) throws Exception {
    if (cls == null || fld == null) {
      throw new java.lang.NullPointerException();
    }
    Field f = cls.getDeclaredField(fld);
    if (f != null) {
      f.setAccessible(true);
      Object r = f.get(null);
      f.setAccessible(false);
      return r;
    }
    else {
      return null;
    }
  }
  

  class MyOracleClob{
    public MyOracleClob(){
      
    }
  //CLOB clob = CLOB.createTemporary(ostmt.getConnection(), true,
    //    CLOB.DURATION_SESSION);
    
    //clob.putChars(1, rr);
    //clob.putString(1, new String(rr));
    //((OraclePreparedStatement) ostmt).setCLOB(parameterIndex, clob);
    public void setClob(PreparedStatement stmt,int i,Reader r) throws SQLException{
      try{
        char[] rr = StmFunc.reader2chars(r, true);
        Object clob = createTemporary(_pstat.getConnection());
        putChars(clob, rr);
        MyOraclePreparedStatement mystat = new MyOraclePreparedStatement(stmt);
        mystat.setClob(i, clob);
      }catch(InvocationTargetException iex){
        //将数据库的异常抛出，比如表空间不足等
        Throwable tex = iex.getTargetException();
        SQLException se = new SQLException(tex.getMessage());
        se.setStackTrace(tex.getStackTrace());
        throw se;
      }catch (Exception ex) {
        SQLException se = new SQLException(ex.getMessage());
        se.setStackTrace(ex.getStackTrace());
        throw se;
      }
    }
    public  Object createTemporary(Connection conn) throws Exception{
      Class clobClass = Class.forName("oracle.sql.CLOB");
      Object ses = getDeclaredStaticField(clobClass, "DURATION_SESSION");
      Class[] params = new Class[] {
          Connection.class, boolean.class, int.class };
      Method createClob = clobClass.getMethod("createTemporary",
          params);
      boolean accessible = createClob.isAccessible();
      createClob.setAccessible(true);
      Object clob = createClob.invoke(null, new Object[] {
          conn, Boolean.TRUE, ses });
      createClob.setAccessible(accessible);
      return clob;
    }
    public void putChars(Object clob,char[] rr) throws Exception{
      Method putmth = clob.getClass().getDeclaredMethod("putChars",
          new Class[] {
              long.class, char[].class });
      boolean accessible = putmth.isAccessible();
      putmth.setAccessible(true);
      putmth.invoke(clob, new Object[] {
          new Long(1), rr });
      putmth.setAccessible(accessible);
    }
  }

  class MyOracleBlob{
    public MyOracleBlob(){
    }
    public void setBlob(PreparedStatement stmt,int i,InputStream x) throws SQLException{
      try {
        byte[] bb = StmFunc.stm2bytes(x);;//可能读取压缩流
        Object blob = createTemporary(_pstat.getConnection());
        putBytes(blob, bb);
        MyOraclePreparedStatement mystat = new MyOraclePreparedStatement(stmt);
        mystat.setBlob(i, blob);
      }catch(InvocationTargetException iex){
        //将数据库的异常抛出，比如表空间不足等
        Throwable tex = iex.getTargetException();
        SQLException se = new SQLException(tex.getMessage());
        se.setStackTrace(tex.getStackTrace());
        throw se;
      }
      catch (Exception ex) {
        SQLException se = new SQLException(ex.getMessage());
        se.setStackTrace(ex.getStackTrace());
        throw se;
      }
    }
    public  Object createTemporary(Connection conn) throws Exception{
      Class blobClass = Class.forName("oracle.sql.BLOB");
      blobClass.getDeclaredFields();
      Object ses = getDeclaredStaticField(blobClass, "DURATION_SESSION");
      Class[] params = new Class[] {
          Connection.class, boolean.class, int.class };
      blobClass.getMethods();
      Method createBlob = blobClass.getMethod("createTemporary",
          params);
      boolean accessible = createBlob.isAccessible();
      createBlob.setAccessible(true);
      Object blob = createBlob.invoke(null, new Object[] {
          conn, Boolean.TRUE, ses });
      createBlob.setAccessible(accessible);
      return blob;
    }

    public void putBytes(Object blob,byte[] bb) throws Exception{
      blob.getClass().getMethods();
      Method putmth = blob.getClass().getMethod("putBytes",
          new Class[] {long.class, byte[].class });
      boolean accessible = putmth.isAccessible();
      putmth.setAccessible(true);
      putmth.invoke(blob, new Object[] {
          new Long(1), bb });
      putmth.setAccessible(accessible);
    }
  }
  class MyOraclePreparedStatement{
    private Object orap;
    private Class pstatClass;
    public MyOraclePreparedStatement(PreparedStatement stmt) {
      try{
        this.orap = getOraPreparedStatementObj(stmt);
        this.pstatClass = Class.forName("oracle.jdbc.OraclePreparedStatement");
      }catch(Exception ex){
        throw new RuntimeException(ex);
      }
     /* this.pstatClass =getSuperClass(orap.getClass(),"oracle.jdbc.driver.OraclePreparedStatement");
      if(pstatClass==null){
        throw new RuntimeException("无法得到oracle.jdbc.driver.OraclePreparedStatement："+orap.getClass().getName());
      }*/
    }
    /*private Class getSuperClass(Class objClass,String classStr){
      String objClassName = objClass.getName();
      if(objClassName.equals(classStr)){
        return objClass;
      }
      Class superClass = objClass.getSuperclass();
      if(superClass!=null){
        return getSuperClass(superClass,classStr);
      }
      return null;
    }*/

    private static final String STMT_ORACLE_CLASSNAME2 =
        "oracle.jdbc.OraclePreparedStatement";
    private static final String STMT_ORACLE_CLASSNAME =
        "oracle.jdbc.driver.OraclePreparedStatement";

    private static final String STMT_WEBPHERE_CLASSNAME =
        "com.ibm.ws.rsadapter.jdbc.WSJdbcPreparedStatement";
    private static final String STMT_WEBLOGIC_CLASSNAME =
        "weblogic.jdbc.rmi.SerialPreparedStatement";
    private static final String STMT_JBOSS_CLASSNAME =
        "org.jboss.resource.adapter.jdbc.WrappedPreparedStatement";
    private static final String STMT_WEBLOGIC81_CLASSNAME =
        "weblogic.jdbc.wrapper.PreparedStatement_oracle_jdbc_driver_OraclePreparedStatement";

    /**
     * 返回Oracle PreparedStatement 实例
     * @param stmt
     * @return
     * @throws Exception
     */
    private Object getOraPreparedStatementObj(PreparedStatement
        stmt) throws Exception {
      Class cls = stmt.getClass();
      String clsname = cls.getName();
      if (clsname.equals(STMT_ORACLE_CLASSNAME2)) {
        return  stmt;
      }
      else if (clsname.equals(STMT_ORACLE_CLASSNAME)) {
        return stmt;
      }
      else if (clsname.equals(STMT_WEBPHERE_CLASSNAME)) {
        Field f = stmt.getClass().getDeclaredField("pstmtImpl");
        f.setAccessible(true);
        Object r =  f.get(stmt);
        f.setAccessible(false);
        return r;
      }
      else if (clsname.equals(STMT_WEBLOGIC_CLASSNAME)) {
        Object o = SysFunc.getDeclaredField(stmt, "rmi_pstmt");
        o = SysFunc.getDeclaredField(o, "t2_pstmt");
        o = SysFunc.getSuperClassDeclaredField(o, "stmt");
        return  o;
      }
      else if (clsname.equals(STMT_WEBLOGIC81_CLASSNAME)) {
        Object o = SysFunc.getSuperSuperClassDeclaredField(stmt, "stmt");
        return  o;
      }
      else if (clsname.equals(STMT_JBOSS_CLASSNAME)) {
        Object o = SysFunc.getDeclaredField(stmt, "ps");
        return  o;
      }
      return stmt;

    }
    public void setClob(int parameterIndex,Object clob) throws Exception{
      Method setCloB = pstatClass.getDeclaredMethod("setCLOB",
          new Class[] {
              int.class, clob.getClass() });
      //Object pstatObject = pstatClass.cast(orap);
      boolean accessible = setCloB.isAccessible();
      setCloB.setAccessible(true);
      setCloB.invoke(orap, new Object[] {
          new Integer(parameterIndex), clob });
      setCloB.setAccessible(accessible);
    }
    
    public void setBlob(int parameterIndex,Object blob) throws Exception{
      Method setBloB = pstatClass.getDeclaredMethod("setBLOB",
          new Class[] {
              int.class, blob.getClass() });
      //Object pstatObject = pstatClass.cast(orap);
      boolean accessible = setBloB.isAccessible();
      setBloB.setAccessible(true);
      setBloB.invoke(orap, new Object[] {
          new Integer(parameterIndex), blob });
      setBloB.setAccessible(accessible);
    }
  }
}
