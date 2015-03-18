package com.esen.jdbc.pool.impl.kingbasees;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;

public class KingBaseESPooledPreparedStatement extends KingBaseESPooledStatement {
  public KingBaseESPooledPreparedStatement(PooledConnection conn,
      PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  
  public KingBaseESPooledPreparedStatement(PooledConnection conn, Statement stat) {
    super(conn,stat);
  }
  
  public ResultSet executeQuery() throws SQLException {
    return new KingBaseESPooledResultSet(_pstat.executeQuery(),pconn);
  }
  
}
