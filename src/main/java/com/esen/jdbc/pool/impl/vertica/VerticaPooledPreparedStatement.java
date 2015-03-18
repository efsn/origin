package com.esen.jdbc.pool.impl.vertica;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;

/**
 * Vertica 数据库的预处理 statement 类
 * 
 * @author liujin
 */
public class VerticaPooledPreparedStatement extends VerticaPooledStatement {

	/**
	 * 构造方法
	 * 
	 * @param conn 数据库连接
	 * @param stat statement
	 */
	public VerticaPooledPreparedStatement(PooledConnection conn, Statement stat) {
		super(conn, stat);
	}

	/**
	 * 构造方法
	 * 
	 * @param conn 数据库连接
	 * @param pstat 预处理statement
	 * @param sql sql语句
	 */
	public VerticaPooledPreparedStatement(PooledConnection conn,
			PreparedStatement pstat, String sql) {
		super(conn, pstat, sql);
	}

	/**
	 * {@inheritDoc}
	 */
	public ResultSet executeQuery() throws SQLException {
		return new VerticaPooledResultSet(_pstat.executeQuery(), pconn);
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setBinaryStream2(int parameterIndex, InputStream x,
			int length) throws SQLException {
		if ((x == null) || (length == 0)) {
			_pstat.setBytes(parameterIndex, null);
			return;
		}
		_pstat.setBinaryStream(parameterIndex, x, length);
	}
}
