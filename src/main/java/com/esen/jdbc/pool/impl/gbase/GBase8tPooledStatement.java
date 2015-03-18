package com.esen.jdbc.pool.impl.gbase;

import java.sql.PreparedStatement;
import java.sql.Statement;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class GBase8tPooledStatement extends PooledPreparedStatement {
	protected GBase8tPooledStatement(PooledConnection conn,
			PreparedStatement pstat, String sql) {
		super(conn, pstat, sql);
	}

	public GBase8tPooledStatement(PooledConnection pconn, Statement stat) {
		super(pconn, stat);
	}
}
