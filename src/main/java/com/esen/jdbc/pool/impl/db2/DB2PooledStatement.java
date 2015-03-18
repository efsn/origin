package com.esen.jdbc.pool.impl.db2;

import java.sql.PreparedStatement;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class DB2PooledStatement extends PooledPreparedStatement {

  protected DB2PooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }

  public DB2PooledStatement(PooledConnection pconn, Statement stat) {
    super(pconn,stat);
  }

}
