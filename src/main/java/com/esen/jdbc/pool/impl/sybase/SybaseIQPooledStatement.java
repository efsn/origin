package com.esen.jdbc.pool.impl.sybase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;

public class SybaseIQPooledStatement extends SybasePooledPreparedStatement {

  public SybaseIQPooledStatement(PooledConnection conn, Statement stat) {
    super(conn, stat);
  }

  public SybaseIQPooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn,pstat,sql);
  }
  
  /**
   * BI-5766 20110926 dw
   * SybaseIQ查询结果集分页，同SybaseAse
   */
  public ResultSet executeQuery(String sql) throws SQLException {
    ResultSet rs = getQureyResultSet(sql);
    return getLimitQeury(sql,new SbaseIQPooledResultSet(rs,pconn));
  }
  
  public ResultSet getGeneratedKeys() throws SQLException {
    return new SbaseIQPooledResultSet(_stat.getGeneratedKeys(),pconn);
  }
  
  public ResultSet getResultSet() throws SQLException {
    return new SbaseIQPooledResultSet(_stat.getResultSet(),pconn);
  }
}
