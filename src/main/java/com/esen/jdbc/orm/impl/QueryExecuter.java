package com.esen.jdbc.orm.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.QueryResult;
import com.esen.jdbc.orm.Session;
import com.esen.util.ArrayFunc;
import com.esen.util.exp.Expression;

public class QueryExecuter<T> extends AbsExecuter implements Query<T> {

	/**
	 * 构造方法
	 * 
	 * @param session 会话
	 * @param entity 实体对象
	 */
	public QueryExecuter(Session session, EntityInfo entity) {
		super(session, entity);
	}

	/**
	 * {@inheritDoc}   
	 */
	public T get(Object idValue, String... propertyNames) {
		Property pk = getPrimaryKey();
		String sql = toQuerySQL(propertyNames, 
						new Expression(pk.getName() + " = ?"),
						null);
		
		try {
			PreparedStatement pstmt = createPreparedStatement(sql);
			try {
				setStatementValue(pstmt, 1, pk, idValue);
				ResultSet rs = pstmt.executeQuery();
				try {
					if (rs.next()) {
						return (T) toObject(rs);
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

		return null;
	}

	/**
	 * {@inheritDoc}   
	 */
	public T get(Object idValue) {
		return get(idValue, new String[]{});
	}
	
	/**
	 * 支持自定义Sql的分页查询，可返回不同于T类型的对象
	 * @param sql
	 * @param params
	 * @return
	 */
	public <K> QueryResult<K> browse(String sql, Object... params) {
		return new QueryResultProxy(new QueryResultImpl(this, sql, params));
	}

	/**
	 * 根据给定的条件，参数值，返回指定属性的对象的结果集
	 * 
	 * @param condition
	 * @param orderbyProperties
	 * @param propertyNames
	 * @param params
	 * @return
	 */
	public QueryResult<T> query(Expression condition, String orderbyProperties, String[] propertyNames, Object... params) {
		String sql = toQuerySQL(propertyNames, condition, orderbyProperties);
		return new QueryResultProxy(new QueryResultImpl(this, sql, params));
	}
	
	/**
	 * 生成查询语句
	 * 
	 * @param properties 属性
	 * @param condition 条件表达式
	 * @param orderbyProperties 排序属性
	 * @return 查询语句
	 */
	protected String toQuerySQL(String[] properties, Expression condition, String orderbyProperties) {
		StringBuilder sql = new StringBuilder();
		sql.append("select ");
		sql.append(ArrayFunc.array2Str(propertiesToSQLField(properties), ','));
		sql.append(" from ").append(entity.getTable());
		
		appendConditionSQL(condition, sql);

		appendOrderbySQL(orderbyProperties, sql);

		return sql.toString();
	}

	/**
	 * 根据 ID 判断指定对象是否存在
	 * 
	 * @param idValue
	 * @return
	 */
	public boolean exist(Object idValue) {
		try {
			Property pk = getPrimaryKey();
			PreparedStatement pstmt = createPreparedStatement(toExistsSQL());
			try {
				setStatementValue(pstmt, 1, pk, idValue);
				ResultSet rs = pstmt.executeQuery();
				try {				
					if (rs.next()) {
						if (rs.getInt(1) > 0) {//生成的sql语句是 SELECT COUNT(*) FROM(select *  from ...)，故判断是否大于0
							return true;//即便if中的条件成立，也要先执行finally中的代码后再返回true。
						}
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
		return false;
	}

	/**
	 * 判断数据存不存在是根据主键来判断的，需要在后面加个条件 where pkfield = ?
	 * @return 生成指定判断对象是否存在的查询 SQL
	 */
	protected String toExistsSQL() {
		Property primaryKey = getPrimaryKey();

		StringBuilder sql = new StringBuilder();
		sql.append("select * ");
		sql.append(" from ").append(entity.getTable());
		sql.append(" where ").append(primaryKey.getFieldName()).append(" = ? ");
		return ((SessionImpl)(this.session)).getDialect().getCountString(sql.toString());
	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> query(Expression condition, String[] propertyNames, Object... params) {
		return query(condition, null, propertyNames, params);
	}

	/**
	 * {@inheritDoc}   
	 */
	public QueryResult<T> list() {
		return query(null, null);
	}
}
