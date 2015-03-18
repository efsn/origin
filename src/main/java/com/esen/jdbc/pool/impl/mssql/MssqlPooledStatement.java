package com.esen.jdbc.pool.impl.mssql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class MssqlPooledStatement extends PooledPreparedStatement {
  protected MssqlPooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }

  public MssqlPooledStatement(PooledConnection pconn, Statement stat) {
    super(pconn,stat);
  }
  public ResultSet executeQuery(String sql) throws SQLException {
    ResultSet rs = super.executeQuery(sql);
    return getLimitQeury(sql, rs);
  }
}
