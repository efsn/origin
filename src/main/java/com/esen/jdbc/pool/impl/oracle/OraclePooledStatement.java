package com.esen.jdbc.pool.impl.oracle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class OraclePooledStatement extends PooledPreparedStatement {

  public OraclePooledStatement(PooledConnection conn, Statement stat) {
    super(conn, stat);
  }
  
  public OraclePooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn,pstat,sql);
  }
  
  public ResultSet executeQuery(String sql) throws SQLException {
    ResultSet rs = getQureyResultSet(sql);
    return new OraclePooledResultSet(rs,pconn);
  }
  
  public ResultSet getGeneratedKeys() throws SQLException {
    return new OraclePooledResultSet(_stat.getGeneratedKeys(),pconn);
  }
  
  public ResultSet getResultSet() throws SQLException {
    return new OraclePooledResultSet(_stat.getResultSet(),pconn);
  }
}
