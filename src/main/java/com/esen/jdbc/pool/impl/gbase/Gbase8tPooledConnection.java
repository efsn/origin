package com.esen.jdbc.pool.impl.gbase;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.pool.DataSourceImpl;
import com.esen.jdbc.pool.PooledConnection;

/**
 * GBASE 8T 数据库默认情况下不支持事务特性, 屏蔽掉自动提交相关方法
 */
public class Gbase8tPooledConnection extends PooledConnection {

	boolean autoCommit;

	public Gbase8tPooledConnection(DataSourceImpl ds, Connection con,
			long connectionIndex) throws SQLException {
		super(ds, con, connectionIndex);
		autoCommit = conn.getAutoCommit();
	}

	public void commit() throws SQLException {
	}

	public boolean getAutoCommit() throws SQLException {
		checkActive();
		return autoCommit;
	}

	public void rollback() throws SQLException {
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		checkActive();
		this.autoCommit = autoCommit;
	}

}
