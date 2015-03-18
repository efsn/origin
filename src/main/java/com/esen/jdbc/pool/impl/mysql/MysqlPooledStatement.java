package com.esen.jdbc.pool.impl.mysql;

import java.sql.PreparedStatement;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class MysqlPooledStatement extends PooledPreparedStatement {
  protected MysqlPooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }

  public MysqlPooledStatement(PooledConnection pconn, Statement stat) {
    super(pconn,stat);
  }
}
