package com.esen.jdbc.pool.impl.oscar;

import java.sql.PreparedStatement;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class OscarPooledStatement extends PooledPreparedStatement {
  protected OscarPooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }

  public OscarPooledStatement(PooledConnection pconn, Statement stat) {
    super(pconn,stat);
  }
}
