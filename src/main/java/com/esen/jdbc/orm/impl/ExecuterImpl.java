package com.esen.jdbc.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Executer;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Session;
import com.esen.util.exp.Expression;
import com.esen.util.reflect.DynaBean;

public class ExecuterImpl<T> extends AbsExecuter<T> implements Executer {

	/**
	 * 构造方法
	 * 
	 * @param session 会话
	 * @param entity 实体对象
	 */
	public ExecuterImpl(Session session, EntityInfo entity) {
		super(session, entity);
	}

	/**
	 * 根据给定的表达式和参数值删除
	 * 
	 * @param condition 条件表达式
	 * @param params 参数值
	 * @return 删除的记录数
	 */
	public int delete(Expression condition, Object[] params) {
		try {
			StringBuilder deleteSql = new StringBuilder();
			deleteSql.append("delete from ").append(entity.getTable());
			
			appendConditionSQL(condition, deleteSql);
			
			PreparedStatement pstmt = createPreparedStatement(deleteSql.toString());
			try {
				for (int i = 0; params != null && i < params.length; i++) {
					setStatementValue(pstmt, i + 1, null, params[i]);
				}
			  return pstmt.executeUpdate();
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * 根据给定的 ID 删除
	 * 
	 * @param idValue 指定的id
	 * @return 删除的记录数
	 */
	public int delete(Object idValue) {
		if (idValue == null) {
			return delete(null, null);
		}
		
		Property pk = getPrimaryKey();
		return delete(new Expression(pk.getName() + " = ?"), new Object[]{idValue});
	}

	/**
	 * 向数据库插入一条记录
	 * 
	 * @param object
	 */
	public void add(Object object) {
		DynaBean<?> bean = DynaBean.getDynaBean(object);
		try {			
			StringBuilder insertSql = new StringBuilder();
			StringBuilder valueSql = new StringBuilder();
			
			String[] fields = propertiesToSQLField(null);
			
			insertSql.append("insert into ").append(entity.getTable()).append("(");
			valueSql.append(" values(");
			
			boolean flag = false;
			for (int i = 0; i < fields.length; i++) {
				if (flag) {
					insertSql.append(", ");
					valueSql.append(", ");
				}
				
				//自动增长列
				if (properties[i].isAutoInc()) {
					continue;
				}
				insertSql.append(fields[i]);
				valueSql.append("?");
				flag = true;
			}
			insertSql.append(")");
			valueSql.append(")");
			
			PreparedStatement pstmt = createPreparedStatement(insertSql.toString() + valueSql.toString());
			try {
				for (int i = 0, index = 0; i < fields.length; i++) {
					Property prop = entity.getPropertyByField(fields[i]);
					
					if (properties[i].isAutoInc()) {
						continue;
					}
					
					index++;
					setStatementValue(pstmt, index, prop,
									ORMUtil.getPropertyValue(entity, bean, prop.getName()));
				}
	
			  pstmt.execute();
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) { //是否单独处理主键冲突异常？
			throw new ORMSQLException(e);
		}
	}
	
	/**
	 * 根据指定的 id 更新部分属性
	 * 
	 * @param oldId 指定的 ID
	 * @param object 新的对象
	 * @param properties 需要更新的属性
	 */
	public void update(Object oldId, Object object, String...properties) {		
		DynaBean<?> bean = DynaBean.getDynaBean(object);
		
		String[] fieldsTmp = propertiesToSQLField(properties);
		String[] fields = null;
		
		Property pk = getPrimaryKey();

		/*
		 * BUG:ESENFACE-1015: modify by liujin 2014.06.13
		 * 如果 id 字段是自增列，有些数据库会出现异常。
		 * 修改为当指定的 id 值和新的对象中的 id 值相同时，SQL 语句中去掉 id 字段的赋值
		 */
		String pkFieldName = pk.getFieldName();
		boolean deletePk = false;
		for (int i = 0; i < fieldsTmp.length; i++) {
			if (pkFieldName.equalsIgnoreCase(fieldsTmp[i])) {
				Property prop = entity.getPropertyByField(fieldsTmp[i]);
				Object newId = ORMUtil.getPropertyValue(entity, bean, prop.getName());
				if ((newId != null && newId.equals(oldId))
						|| (newId == null && oldId == null)) {
					deletePk = true;
					fields = new String[fieldsTmp.length - 1];
					for (int j = 0, k = 0; j < fieldsTmp.length; j++) {
						if (j != i) {
							fields[k] = fieldsTmp[j];
							k++;
						}
					}
				}
				break;
			}
		}
		if (!deletePk) { //需要更新的字段中不需去除主键
			fields = fieldsTmp;
		}

		if (fields.length == 0) { //没有需要更新的字段时，直接返回
			return;
		}
		StringBuilder updateSql = new StringBuilder();
		updateSql.append("update ").append(entity.getTable());
		updateSql.append(" set ");

		for (int i = 0; i < fields.length; i++) {
			if (i != 0) {
				updateSql.append(", ");
			}
			updateSql.append(fields[i]).append("= ? ");
		}

		updateSql.append(" where " + pk.getFieldName() + " = ? ");
		
		try {
			PreparedStatement pstmt = createPreparedStatement(updateSql.toString());
			try {
				int i = 0;
				for (; i < fields.length; i++) {
					Property prop = entity.getPropertyByField(fields[i]);
					setStatementValue(pstmt, i + 1, prop, ORMUtil.getPropertyValue(entity, bean, prop.getName()));
				}
				//pk value
				setStatementValue(pstmt, i + 1, pk, oldId);
				//执行
			  pstmt.executeUpdate();
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

}
