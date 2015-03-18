package com.esen.jdbc.orm.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.orm.Iterator;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.QueryResult;
import com.esen.util.ExceptionHandler;

/**
 * 
 *
 * @author wang
 */
public class QueryResultImpl<T> implements QueryResult<T> {
	
	/**
	 * sql 语句
	 */
	private String sql;
	
	/**
	 * 参数值
	 */
	private Object[] params;

	/**
	 * 查询执行器
	 */
	protected QueryExecuter queryExe;
	
	/**
	 * 构造方法
	 * 
	 * @param queryExe 查询执行器
	 * @param sql sql语句
	 * @param params 参数值
	 */
	public QueryResultImpl(QueryExecuter queryExe, String sql, Object[] params) {
		this.queryExe = queryExe;
		this.sql = sql;
		this.params = params;
	}

	/**
	 * {@inheritDoc}   
	 */
	public int calcTotalCount() {
		String calcSql = ((SessionImpl)(queryExe.session)).getDialect().getCountString(sql);
		int rows = 0;

		try {
			PreparedStatement pstmt = queryExe.createPreparedStatement(calcSql);
			try {
				if (params != null && params.length >= 1) {
					for (int i = 0; i < params.length; i++) {
						queryExe.setStatementValue(pstmt, i + 1, null, params[i]);
					}
				}
				
				ResultSet rs = pstmt.executeQuery();
				try {
					if (rs.next()) {
						rows = rs.getInt(1);
					}
				} finally {
					rs.close();
				}
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) {
			//TODO 此处修改为了在出现"连接关闭"的异常时方便定位问题，待问题解决后代码需恢复原样。
			//throw new ORMSQLException(e);
			ExceptionHandler.rethrowRuntimeException(e);
		}

		return rows;
	}

	/**
	 * {@inheritDoc}   
	 */
	public List list(int pageIndex, int pageSize) {
		int offset = -1;
		if (pageIndex >= 0 && pageSize >= 0) {
			offset = pageIndex * pageSize;
		}
		
		String selectSql = ((SessionImpl)(queryExe.session)).getDialect().getLimitString(sql, offset, pageSize);
		List resultList = new ArrayList();
		try {
			PreparedStatement pstmt = queryExe.createPreparedStatement(selectSql);
			try {
				if (params != null && params.length >= 1) {
					for (int i = 0; i < params.length; i++) {
						queryExe.setStatementValue(pstmt, i + 1, null, params[i]);
					}
				}
				
				ResultSet rs = pstmt.executeQuery();
				try {
					while (rs.next()) {
						resultList.add(queryExe.toObject(rs));
					}
				} finally {
					rs.close();
				}
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}

		return resultList;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Iterator iterator(int pageIndex, int pageSize) {
		int offset = -1;
		if (pageIndex >= 0 && pageSize >= 0) {
			offset = pageIndex * pageSize;
		}

		String selectSql = ((SessionImpl)(queryExe.session)).getDialect().getLimitString(sql, offset, pageSize);

		return new IteratorImpl(queryExe, selectSql, params);
	}
	
	protected String getSql() {
		return sql;
	}
	
	protected Object[] getParams() {
		return params;
	}
}
