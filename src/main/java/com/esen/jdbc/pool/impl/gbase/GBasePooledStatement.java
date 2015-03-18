package com.esen.jdbc.pool.impl.gbase;

import java.sql.PreparedStatement;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class GBasePooledStatement extends PooledPreparedStatement {
  protected GBasePooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }

  public GBasePooledStatement(PooledConnection pconn, Statement stat) {
    super(pconn,stat);
  }
}
