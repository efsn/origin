package com.esen.jdbc.pool.impl.oscar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;
import com.esen.util.StmFunc;

public class OscarPooledPreparedStatement extends OscarPooledStatement {

  public OscarPooledPreparedStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  protected void setBinaryStream2(int parameterIndex, InputStream x, int length) throws SQLException {
    if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
      pconn.logDebug(parameterIndex + "  " + x + "  " + length);
    if ( (x == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.BLOB);
      return;
    }
    Connection conn = pconn.getSourceConnection();
    Blob blob = null;
    try {
       //Class osc = Class.forName("com.oscar.jdbc.OscarJdbc2Connection");
       //Class oscon = osc.asSubclass(conn.getClass());
       Method mth = conn.getClass().getDeclaredMethod("createBlob", null);
       boolean accessible = mth.isAccessible();
       mth.setAccessible(true);
       blob = (Blob)mth.invoke(conn, null);
       mth.setAccessible(accessible);
    }
    catch (Exception e) {
      if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
        pconn.logError(e, "");
    }
    
    //Blob blob = ( (OscarJdbc2Connection) conn).createBlob();
    OutputStream os = blob.setBinaryStream(1);
    try {
      StmFunc.stmTryCopyFrom(x, os);
      os.flush();
    }
    catch (IOException e) {
      if(pconn.canLogLevel(JdbcLogger.LOG_LEVER_ERROR))
        pconn.logError(e, "");
    }
    _pstat.setBlob(parameterIndex, blob);
  }
  protected void setCharacterStream2(int parameterIndex, Reader reader, int length) throws
  SQLException {
    if ( (reader == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.CLOB);
      return;
    }
    String ss = getStrFromReader(reader);
    _pstat.setString(parameterIndex, ss);
  }
}
