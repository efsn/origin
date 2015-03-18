package com.esen.jdbc.pool.impl.teradata;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

/**
 * Teradata 数据库的 statement 类
 *
 * @author liujin
 */
public class TeradataPooledStatement extends PooledPreparedStatement {

	/**
	 * 构造方法
	 * @param conn 数据库连接
	 * @param stat statement
	 */
	public TeradataPooledStatement(PooledConnection conn, Statement stat) {
		super(conn, stat);
	}

	/**
	 * 构造方法
	 * @param conn 数据库连接
	 * @param pstat 预处理 statement
	 * @param sql sql 语句
	 */
	public TeradataPooledStatement(PooledConnection conn,
			PreparedStatement pstat, String sql) {
		super(conn, pstat, sql);
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		ResultSet rs = getQureyResultSet(sql);
		return new TeradataPooledResultSet(rs, pconn);
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultSet getGeneratedKeys() throws SQLException {
		return new TeradataPooledResultSet(_stat.getGeneratedKeys(), pconn);
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultSet getResultSet() throws SQLException {
		return new TeradataPooledResultSet(_stat.getResultSet(), pconn);
	}

}
