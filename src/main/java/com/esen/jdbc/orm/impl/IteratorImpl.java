package com.esen.jdbc.orm.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.esen.jdbc.orm.Iterator;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.util.StrFunc;

/**
 * 
 *
 * @author wang
 */
public class IteratorImpl<T> implements Iterator {
	
	/**
	 * 结果集
	 */
	private ResultSet rs;
	
	/**
	 * PreparedStatement
	 */
	private PreparedStatement pstmt;
	
	/**
	 * 
	 */
	private QueryExecuter queryExe;
	
	/**
	 * 
	 */
	public IteratorImpl(QueryExecuter queryExe, String sql, Object[] params) {
		if (queryExe == null
				|| StrFunc.isNull(sql)) {
			throw new ORMException("com.esen.jdbc.orm.impl.iteratorimpl.1","参数不能为空");
		}
		
		this.queryExe = queryExe;
		try {
			pstmt = queryExe.createPreparedStatement(sql);
			pstmt.setFetchSize(10);
			
			for(int i = 1; params != null && i <= params.length; i++) {
				queryExe.setStatementValue(pstmt, i, null, params[i - 1]);
			}
			rs = pstmt.executeQuery();
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * {@inheritDoc}   
	 */
	public T next() {		
		return (T) queryExe.toObject(rs);
	}

	/**
	 * {@inheritDoc}   
	 */
	public void close() {
		try {
			if (rs != null) {
				rs.close();
			}
			
			if (pstmt != null) {
				pstmt.close();
			}
		} catch(SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean hasNext() {
		try {
			return rs.next();
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}
}
