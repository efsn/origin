package com.esen.jdbc.orm.helper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.impl.SessionImpl;
import com.esen.util.IProgress;
import com.esen.util.StrFunc;

/**
 * 实体工具对象
 * @param <T> javabean<T>对象
 *
 * @author wang
 */
public class EntityAdvFunc extends EntityDataManager {

	/**
	 * 检查并修复表结构
	 * @param ipro
	 */
	public final static void repairTable(Session session, String entityName, IProgress ipro) {
		if (session == null || StrFunc.isNull(entityName)) {
			throw new ORMException("com.esen.jdbc.orm.helper.entityadvfunc.1","参数不能为空");
		}

		EntityInfo entityInfo = ((SessionImpl) session).getEntityInfo(entityName);
		Document xml = getEntityDocument(entityInfo);

		try {
			Connection conn = session.getConnection();
			try {
				DbDefiner def = ((SessionImpl) session).getDbDefiner();
				def.repairTable(conn, xml);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * 升级数据库表结构
	 * @param entityName 升级的实体名称
	 * @param session ORM、session
	 * @param newEntity 修改后的newEntity
	 * @param nameMapping 如果修改了属性（字段）名称，namingMapping中存放映射关系。
	 * 					为 Map<oldPropertyName, newPropertyName>
	 * @param ipro 进度条
	 */
	public final static void upgradeTable(Session session, String entityName, EntityInfo newEntity,
			Map<String, String> nameMapping, IProgress ipro) {
		if (newEntity == null || session == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.entityadvfunc.1","参数不能为空");
		}

		if (StrFunc.isNull(entityName)) {
			entityName = newEntity.getEntityName();
		}
		if (StrFunc.isNull(entityName)) {
			throw new ORMException("com.esen.jdbc.orm.helper.entityadvfunc.2","实体名称不能为空");
		}
		EntityInfo oldEntity = ((SessionImpl) session).getEntityInfo(entityName);
		//旧实体信息不存在，直接处理新实体信息
		if (oldEntity == null) {
			repairTable(session, newEntity.getEntityName(), ipro);
			return;
		}
		
		String oldTableName = oldEntity.getTable();
		String newTableName = newEntity.getTable();
		
		DbDefiner def = ((SessionImpl) session).getDbDefiner();
		
		try {
			Connection conn = session.getConnection();
			
			//修改表名
			def.renameTable(conn, oldTableName, newTableName);

			try {
				/*
				 * 如果存在属性名称的修改，先根据属性名称相关信息修改表结构
				 * 如果直接修复表结构时，改名的字段的操作为删除旧字段，添加新字段
				 * 
				 * 注意：如果同时存在 A 和  B，需要将A修改为B，将B修改为C，可能会处理不正确
				 * 	
				 */
				if (nameMapping != null && nameMapping.size() > 0) {
					Set<String> key = nameMapping.keySet();
					for (java.util.Iterator it = key.iterator(); it.hasNext();) {
						String oldname = (String) it.next();
						String newname = nameMapping.get(oldname);
	
						Property oldProp = oldEntity.getProperty(oldname);
						Property newProp = newEntity.getProperty(newname);
	
						def.modifyColumn(conn, newTableName, oldProp.getFieldName(), newProp.getFieldName(), newProp.getType(),
								newProp.length(), newProp.getScale(), null, newProp.isUnique(), newProp.isNullable());
					}
				}
				
				//最后处理其他修改了的信息
				repairTable(session, newEntity.getEntityName(), ipro);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * 创建数据库表
	 * 
	 * @param session
	 * @param entityInfo
	 */
	public final static void createTable(Session session, EntityInfo entityInfo) {
		if (entityInfo == null || session == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.entityadvfunc.1","参数不能为空");
		}

		Document xml = getEntityDocument(entityInfo);

		try {
			Connection conn = session.getConnection();
			try {
				DbDefiner def = ((SessionImpl) session).getDbDefiner();
				def.createTable(conn, xml, null, false, true);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * 删除数据库表
	 * 
	 * @param session 会话
	 * @param entityName 删除的实体名称
	 */
	public final static void dropTable(Session session, String entityName) {
		if (StrFunc.isNull(entityName) || session == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.entityadvfunc.1","参数不能为空");
		}

		EntityInfo entity = ((SessionImpl) session).getEntityInfo(entityName);
		try {
			Connection conn = session.getConnection();
			try {
				DbDefiner def = ((SessionImpl) session).getDbDefiner();
				def.dropTable(conn, null, entity.getTable());
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * 把实体对象转换成  DbDefiner 可以识别的 xml 文件
	 * 
	 * @param entityInfo
	 * @return
	 */
	private static Document getEntityDocument(EntityInfo entityInfo) {
		return XmlWriter.entityToDbdfinerDoc(entityInfo);
	}
	
	/**
	 * 判断该表是否存在
	 * 
	 * @param session 会话
	 * @param tablename 表名
	 * @return 表是否存在
	 */
	public static boolean isTableExist(Session session, String tablename) {
		if (session == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.entityadvfunc.1","参数不能为空");
		}
		
		try {
			Connection conn = session.getConnection();
			try {
				DbDefiner def = ((SessionImpl) session).getDbDefiner();
				return def.tableExists(conn, null, tablename);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}
}
