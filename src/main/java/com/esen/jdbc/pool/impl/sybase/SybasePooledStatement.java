package com.esen.jdbc.pool.impl.sybase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class SybasePooledStatement extends PooledPreparedStatement {
  protected SybasePooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }

  public SybasePooledStatement(PooledConnection pconn, Statement stat) {
    super(pconn,stat);
  }
  
  public ResultSet executeQuery(String sql) throws SQLException {
    ResultSet rs = getQureyResultSet(sql);
    return getLimitQeury(sql, new SybasePooledResultSet(rs,pconn));
  }
  
  public ResultSet getGeneratedKeys() throws SQLException {
    return new SybasePooledResultSet(_stat.getGeneratedKeys(),pconn);
  }
  
  public ResultSet getResultSet() throws SQLException {
    return new SybasePooledResultSet(_stat.getResultSet(),pconn);
  }
}
