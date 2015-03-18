package com.esen.jdbc.pool.impl.vertica;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class VerticaPooledStatement extends PooledPreparedStatement {

	/**
	 * 构造方法
	 * @param conn 数据库连接
	 * @param stat statement
	 */
	public VerticaPooledStatement(PooledConnection conn, Statement stat) {
		super(conn, stat);
	}

	/**
	 * 构造方法
	 * @param conn 数据库连接
	 * @param pstat 预处理 statement
	 * @param sql sql 语句
	 */
	public VerticaPooledStatement(PooledConnection conn,
			PreparedStatement pstat, String sql) {
		super(conn, pstat, sql);
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultSet executeQuery(String sql) throws SQLException {
		ResultSet rs = getQureyResultSet(sql);
		return new VerticaPooledResultSet(rs, pconn);
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultSet getResultSet() throws SQLException {
		return new VerticaPooledResultSet(_stat.getResultSet(), pconn);
	}
}
