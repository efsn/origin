package com.esen.jdbc.pool.impl.oracle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;

/**
 * 20090805
 * 对Oracle 8 使用单独的Statement；
 * 查询后获得结果集也是Oracle8特有的，因为读取大字段的原因；
 * @author Administrator
 *
 */
public class Oracle8PooledStatement extends OraclePooledPreparedStatement {

  public Oracle8PooledStatement(PooledConnection conn, Statement stat) {
    super(conn, stat);
  }
  public Oracle8PooledStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }

  public ResultSet executeQuery(String sql) throws SQLException {
    ResultSet rs = getQureyResultSet(sql);
    return new Oracle8PooledResultSet(rs,pconn);
  }
  
  public ResultSet getGeneratedKeys() throws SQLException {
    return new Oracle8PooledResultSet(_stat.getGeneratedKeys(),pconn);
  }
  
  public ResultSet getResultSet() throws SQLException {
    return new Oracle8PooledResultSet(_stat.getResultSet(),pconn);
  }
}
