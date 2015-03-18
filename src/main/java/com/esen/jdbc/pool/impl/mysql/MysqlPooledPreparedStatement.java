package com.esen.jdbc.pool.impl.mysql;

import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.pool.PooledConnection;

public class MysqlPooledPreparedStatement extends MysqlPooledStatement {

  public MysqlPooledPreparedStatement(PooledConnection conn,
      PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  
  protected void setCharacterStream2(int parameterIndex, Reader reader,
      int length) throws SQLException {
    if ((reader == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.LONGVARCHAR);
      return;
    }
    //length长度可能是很大一个值
    String str = getStrFromReader(reader);
    _pstat.setString(parameterIndex, str);
  }
  protected void setBinaryStream2(int parameterIndex, InputStream x, int length)
      throws SQLException {
    if ((x == null) || (length == 0)) {
      _pstat.setNull(parameterIndex, java.sql.Types.LONGVARBINARY);
      return;
    }
    _pstat.setBinaryStream(parameterIndex, x, length);
  }
}
