package com.esen.jdbc.pool.impl.sybase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.esen.jdbc.pool.PooledConnection;

public class SybaseIQPooledPreparedStatement extends
        SybaseIQPooledStatement {

  public SybaseIQPooledPreparedStatement(PooledConnection conn,
      PreparedStatement pstat, String sql) {
    super(conn, pstat,sql);
  }
  public ResultSet executeQuery() throws SQLException {
    return new SbaseIQPooledResultSet(_pstat.executeQuery(),pconn);
  }
}
