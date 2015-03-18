package com.esen.jdbc.pool.impl.teradata;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;

/**
 * Teradata 数据库的预处理 statement 类 
 *
 * @author liujin
 */
public class TeradataPooledPreparedStatement extends TeradataPooledStatement {

	/**
	 * 构造方法
	 * @param conn 数据库连接
	 * @param stat statement
	 */
  public TeradataPooledPreparedStatement(PooledConnection conn, Statement stat) {
    super(conn, stat);
  }

  /**
   * 构造方法
   * @param conn 数据库连接
   * @param pstat 预处理statement
   * @param sql sql语句
   */
  public TeradataPooledPreparedStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  
  /**
   * {@inheritDoc}
   */
  public ResultSet executeQuery() throws SQLException {
    return new TeradataPooledResultSet(_pstat.executeQuery(), pconn);
  }
}
