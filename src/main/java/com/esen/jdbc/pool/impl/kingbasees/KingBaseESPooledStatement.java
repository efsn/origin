package com.esen.jdbc.pool.impl.kingbasees;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class KingBaseESPooledStatement extends PooledPreparedStatement {

  protected KingBaseESPooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  public KingBaseESPooledStatement(PooledConnection pconn, Statement stat) {
    super(pconn,stat);
  }
  
  public ResultSet executeQuery(String sql) throws SQLException {
    ResultSet rs = getQureyResultSet(sql);
    return new KingBaseESPooledResultSet(rs,pconn);
  }
  
  public ResultSet getGeneratedKeys() throws SQLException {
    return new KingBaseESPooledResultSet(_stat.getGeneratedKeys(),pconn);
  }
  
  public ResultSet getResultSet() throws SQLException {
    return new KingBaseESPooledResultSet(_stat.getResultSet(),pconn);
  }
  
}
