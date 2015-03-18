package com.esen.jdbc.pool.impl.gbase;

import java.io.InputStream;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.pool.PooledConnection;

public class GBase8tPooledPreparedStatement extends GBase8tPooledStatement {

	public GBase8tPooledPreparedStatement(PooledConnection conn,
			PreparedStatement pstat, String sql) {
		super(conn, pstat, sql);
	}

	protected void setCharacterStream2(int parameterIndex, Reader reader,
			int length) throws SQLException {
		if ((reader == null) || (length == 0)) {
			_pstat.setNull(parameterIndex, java.sql.Types.LONGVARCHAR);
			return;
		}
		String str = getStrFromReader(reader);
		_pstat.setString(parameterIndex, str);
	}

	protected void setBinaryStream2(int parameterIndex, InputStream x,
			int length) throws SQLException {
		if ((x == null) || (length == 0)) {
			_pstat.setNull(parameterIndex, java.sql.Types.LONGVARBINARY);
			return;
		}
		_pstat.setBinaryStream(parameterIndex, x, -1);
	}

	public int executeUpdate() throws SQLException {
		_pstat.addBatch();
		_pstat.executeBatch();
		return 1;
	}
}
