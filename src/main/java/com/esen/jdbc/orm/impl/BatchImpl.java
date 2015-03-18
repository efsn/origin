package com.esen.jdbc.orm.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.esen.jdbc.orm.Batch;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.Executer;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Session;
import com.esen.util.reflect.DynaBean;

/**
 * 
 *
 * @author wang
 */
public class BatchImpl<T> implements Batch<T> {

	/**
	 * PreparedStatement
	 */
	private PreparedStatement pstmt;
	
	/**
	 * 需要操作的属性名
	 */
	private Property[] properties;

	/**
	 * 实体对象
	 */
	private EntityInfo entity;
	
	/**
	 * 更新操作执行器
	 */
	private Executer updateExe;
	
	/**
	 * 批量操作的 SQL 的类型
	 */
	private int batchType;
	
	/**
	 * 构造方法
	 * 
	 * @param owner 会话
	 * @param entity 实体对象
	 * @param batchType 批量操作类型，包括 insert 和  update
	 * @param properties 需要操作的属性
	 */
	public BatchImpl(Session owner, EntityInfo entity, int batchType, String...properties) {	
		String[] fields;
		this.entity = entity;
		if (properties != null && properties.length > 0) {
			fields = new String[properties.length];
			this.properties = new Property[properties.length];
			for (int i = 0; i < properties.length; i++) {
				this.properties[i] = entity.getProperty(properties[i]);
				if (this.properties[i] != null) {
					fields[i] = this.properties[i].getFieldName();
				} else {
					throw new ORMException("com.esen.jdbc.orm.impl.batchimpl.2", "属性名{0}不存在", new Object[]{properties[i]});
				}
			}
		} else { // properties 为空，表示全表
			List props = entity.getProperties();
			int size = props.size();
			fields = new String[size];
			this.properties = new Property[size];
			boolean hasAutoInc = false;
			for (int i = 0; i < size; i++) {
				this.properties[i] = (Property) props.get(i);
				fields[i] = ((Property)props.get(i)).getFieldName();
				if (this.properties[i].isAutoInc() && batchType == Batch.BATCHTYPE_INSERT) {
					hasAutoInc = true;
					break;
				}
			}
			
			//批量 Insert 操作，不处理自增长字段
			if (hasAutoInc) {
				fields = new String[size - 1];
				this.properties = new Property[size - 1];
				for (int i = 0, j = 0; i < size; i++) {
					this.properties[j] = (Property) props.get(i);
					if (!this.properties[j].isAutoInc()) {
						fields[j] = ((Property)props.get(i)).getFieldName();
						j++;
					}
				}
			}
		}
			
		StringBuilder sql = new StringBuilder();
		
		this.batchType = batchType;
		switch (batchType) {
		case Batch.BATCHTYPE_UPDATE: { // update 类型 batch 操作
			sql.append("update ").append(entity.getTable()).append(" set ");
			for (int i = 0; i < fields.length; i++) {
				if (i != 0) {
					sql.append(", ");
				}
				sql.append(fields[i]).append(" = ?");
			}
			sql.append(" where ").append(entity.getPrimaryKey().getFieldName()).append(" = ? ");
			break;
		}
			
		case Batch.BATCHTYPE_INSERT: { // insert 类型 batch 操作
			StringBuilder value = new StringBuilder();
			
			sql.append("insert into ").append(entity.getTable()).append("(");
			value.append("values (");
			
			for (int i = 0; i < fields.length; i++) {
				if (i != 0) {
					sql.append(", ");
					value.append(", ");
				}
				sql.append(fields[i]);
				value.append("?");
			}			
			sql.append(") ").append(value.toString()).append(")");
			break;
		}
			
		case Batch.BATCHTYPE_DELETE: { // delete 类型 Batch 操作
			sql.append("delete from ").append(entity.getTable());
			sql.append(" where ");
			for (int i = 0; i < fields.length; i++) {
				if (i != 0) {
					sql.append(" and ");
				}
				sql.append(fields[i]).append(" = ?");
			}

			break;
		}
		
		default:
			throw new ORMSQLException("com.esen.jdbc.orm.impl.batchimpl.1","批量操作类型错误");
		}
		
		updateExe = owner.createExecuter(entity.getClass(), entity.getEntityName());
		try {
			pstmt = ((ExecuterProxy)updateExe).createPreparedStatement(sql.toString());
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/** 
	 * {@inheritDoc}   
	 */
	public void addBatch(Object object) {
		DynaBean<?> bean = DynaBean.getDynaBean(object);
		try {
			int i = 0;
			for (i = 0; i < properties.length; i++) {
				Object value = ORMUtil.getPropertyValue(entity, bean, properties[i].getName());
				((ExecuterProxy)updateExe).setStatementValue(pstmt, i + 1, properties[i], value);
			}
			if (this.batchType == Batch.BATCHTYPE_UPDATE) { //更新的时候，SQL 语句中多一个参数 key
				Object value = ORMUtil.getPropertyValue(entity, bean, entity.getPrimaryKey().getName());
				((ExecuterProxy)updateExe).setStatementValue(pstmt, i + 1, entity.getPrimaryKey(), value);
			}
			pstmt.addBatch();
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * {@inheritDoc}   
	 */
	public void exectue() {
		try {
			pstmt.executeBatch();//这里不能用execute()
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public void close() {
		try {
			pstmt.close();
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

}
