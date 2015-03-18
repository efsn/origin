package com.esen.jdbc.pool.impl.dm;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.pool.JdbcLogger;
import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class DMPooledPreparedStatement extends PooledPreparedStatement {

	public DMPooledPreparedStatement(PooledConnection conn, PreparedStatement pstat, String sql) {
		super(conn, pstat, sql);
	}

	public void setString(int parameterIndex, String x) throws SQLException {
		if (x == null) {
		  if (pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
		    pconn.logDebug(parameterIndex + " null " + java.sql.Types.VARCHAR);
			_pstat.setNull(parameterIndex, java.sql.Types.VARCHAR);
			return;
		}
		super.setString(parameterIndex, x);
	}
	
	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
		if (x == null) {
		  if (pconn.canLogLevel(JdbcLogger.LOG_LEVER_DEBUG))
		    pconn.logDebug(parameterIndex + " null " + java.sql.Types.BINARY);
			_pstat.setNull(parameterIndex, java.sql.Types.BINARY);
			return;
		}
		super.setBytes(parameterIndex, x);
	}

}
