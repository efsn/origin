package com.esen.jdbc.pool.impl.vertica;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledResultSet;

public class VerticaPooledResultSet extends PooledResultSet {

	/**
	 * 构造方法
	 * @param rs 结果集
	 * @param pconn 数据库连接
	 * @throws SQLException 构造 Vertica 数据库结果集对象出现异常时抛出该异常
	 */
	public VerticaPooledResultSet(ResultSet rs, PooledConnection pconn)
			throws SQLException {
		super(rs, pconn);
	}

}
