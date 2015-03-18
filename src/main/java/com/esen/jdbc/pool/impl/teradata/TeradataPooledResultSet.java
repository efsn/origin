package com.esen.jdbc.pool.impl.teradata;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledResultSet;

/**
 * Teradata 数据库结果集
 *
 * @author liujin
 */
public class TeradataPooledResultSet extends PooledResultSet {

	/**
	 * 构造方法
	 * @param rs 结果集
	 * @param pconn 数据库连接
	 * @throws SQLException 构造 Teradata 数据库结果集对象出现异常时抛出该异常
	 */
	public TeradataPooledResultSet(ResultSet rs, PooledConnection pconn)
			throws SQLException {
		super(rs, pconn);
	}
  
	/**
	 * {@inheritDoc}
	 */
	public String getString(int columnIndex) throws SQLException {
		switch (getSQLType(columnIndex)) {
		case Types.CLOB:
			Clob lob = super.getClob(columnIndex);
			if (null == lob) {
				return null;
			}
			else {
				return lob.getSubString(1, (int)lob.length());
			}
		
		default:
			return super.getString(columnIndex);
		}
	}
  
	/**
	 * {@inheritDoc}
	 */
	public String getString(String columnName) throws SQLException {
		switch (getColumnType(columnName)) {
		case Types.CLOB:
			Clob lob = super.getClob(columnName);
			if (null == lob) {
				return null;
			}
			else {
				return lob.getSubString(1, (int)lob.length());
			}

		default:
			return super.getString(columnName);
		}
	}
  
	/**
	 * {@inheritDoc}
	 */
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		switch (getSQLType(columnIndex)) {
		case Types.BLOB:
			Blob lob = super.getBlob(columnIndex);
			if (null == lob) {
				return null;
			}
			else {
				return lob.getBinaryStream();
			}
		
		default:
			return super.getBinaryStream(columnIndex);
		}
	}
  
	/**
	 * {@inheritDoc}
	 */
	public InputStream getBinaryStream(String columnName) throws SQLException {
		switch (getColumnType(columnName)) {
		case Types.BLOB:
			Blob lob = super.getBlob(columnName);
			if (null == lob) {
				return null;
			}
			else {
				return lob.getBinaryStream();
			}
		
		default:
			return super.getBinaryStream(columnName);
		}
	}

}
