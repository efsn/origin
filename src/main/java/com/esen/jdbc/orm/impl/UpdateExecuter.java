package com.esen.jdbc.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.Update;
import com.esen.util.StrFunc;
import com.esen.util.exp.Expression;

/**
 * Update 实现类
 *
 * @author wang
 */
public class UpdateExecuter<T> implements Update<T> {

	/**
	 * 更新执行器
	 */
	private ExecuterImpl<T> executer;

	/**
	 * 
	 */
	private EntityInfo entity;

	/**
	 * 需要更新的字段和值，不包括使用表达式的
	 */
	private HashMap<String, Object> fieldValues;

	/**
	 * 需要更新的字段和表达式
	 */
	private HashMap<String, Expression> fieldExps;

	/**
	 * update 语句的  where 子句部分
	 */
	private String cond;

	/**
	 * update 语句的 where 子句部分的参数值
	 */
	private List<Object> whereParams = new ArrayList<Object>();

	/**
	 * PreparedStatement
	 */
	private PreparedStatement pstmt;

	/**
	 * 构造方法
	 * 
	 * @param session 会话
	 * @param entity 实体对象
	 */
	public UpdateExecuter(Session session, EntityInfo entity) {
		this.executer = new ExecuterImpl(session, entity);
		this.entity = entity;
		this.fieldValues = new HashMap();
		this.fieldExps = new HashMap();
	}

	/**
	 * 获取实体信息
	 * 
	 * @return
	 */
	protected EntityInfo getEntityInfo() {
		return this.entity;
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public Update setCondition(Expression conditionExp, Object... params) {
		if (conditionExp == null 
				|| StrFunc.isNull(conditionExp.toString())) {
			return this;
		}
		
		if (StrFunc.isNull(cond)) {
			cond = "(" + conditionExp.toString() + ")";
		} else {
			//多个表达式时，用  and 连接
			cond = "(".concat(cond).concat(" and (").concat(conditionExp.toString()).concat("))");
		}

		for (int i = 0; params != null && i < params.length; i++) {
			whereParams.add(params[i]);
		}

		return this;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Update setPropertyValue(String property, Object value) {
		Property prop = entity.getProperty(property);
		if (prop == null) {
			throw new ORMException("com.esen.jdbc.orm.impl.updateexecuter.1","属性{0}不存在",new Object[]{property});
		}
		
		String field = prop.getFieldName();

		//删除先前使用字段的表达式设置参数值的
		if (fieldExps.containsKey(field)) {
			fieldExps.remove(field);
		}

		fieldValues.put(field, value);

		return this;
	}

	/**
	 * {@inheritDoc}   
	 */
	public Update setPropertyExp(String property, Expression exp) {
		Property prop = entity.getProperty(property);
		if (prop == null) {
			throw new ORMException("com.esen.jdbc.orm.impl.updateexecuter.2","属性{0}不存在",new Object[]{property});
		}
		
		String field = prop.getFieldName();

		//删除先前使用字段的值设置参数值的
		if (fieldValues.containsKey(field)) {
			fieldValues.remove(field);
		}
		fieldExps.put(field, exp);

		return this;
	}

	/**
	 * {@inheritDoc}   
	 */
	public int executeUpdate() {
		int ret = 0;

		//没有需要更新的字段
		if (fieldValues.size() == 0
				&& fieldExps.size() == 0) {
			return 0;
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("update ").append(entity.getTable()).append(" set ");

		Iterator it = fieldValues.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			sql.append(" ").append(key).append(" = ?,");
		}

		it = fieldExps.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			
			//表达式需要进行编译后拼入 sql 语句
			sql.append(" ").append(key).append(" = ")
				.append(executer.getSql(fieldExps.get(key)))
				.append(",");
		}

		//删除最后一个逗号
		sql.deleteCharAt(sql.length() - 1);

		executer.appendConditionSQL(new Expression(cond), sql);

		try {
			pstmt = executer.createPreparedStatement(sql.toString());
			try {
				int index = 1;

				//设置参数值
				//set 子句中的
				it = fieldValues.keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					executer.setStatementValue(pstmt, index, entity.getPropertyByField(key), fieldValues.get(key));
					index++;
				}

				// where 子句中的
				for (int i = 0; i < whereParams.size(); i++) {
					executer.setStatementValue(pstmt, index, null, whereParams.get(i));
					index++;
				}

				ret = pstmt.executeUpdate();
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}

		return ret;
	}
	
	/**
	 * 获取连接池的名字
	 * 
	 * @return 连接池的名字
	 */
	public String getConnName() {
		return this.executer.getConnName();
	}
}
