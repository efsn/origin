package com.esen.jdbc.orm.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.orm.Batch;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.EntityInfoManager;
import com.esen.jdbc.orm.Executer;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Query;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.Update;
import com.esen.util.StrFunc;
import com.esen.util.exp.Expression;
import com.esen.util.reflect.DynaBean;

public class SessionImpl implements Session {

	/**
	 * 数据库连接池名
	 */
	/*
	 * BUG：ESENFACE-2063,ESENBI-3199,ESENBI-3650: modify by liujin 2014.12.30
	 * 改为使用数据库连接池的名字，避免连接池重启以后，
	 * 持有的 connfactory 失效，导致出现连接池已被关闭的错误。
	 */
	//private ConnectionFactory connfactory;
	private String dsname;

	/**
	 * 是否在一个事务里面
	 */
	private boolean isTransaction;

	/**
	 * 实体对象管理
	 */
	private EntityInfoManager entityManager;

	/**
	 * 数据库连接
	 */
	private ORMConnection conn;

	/**
	 * 获得数据定义接口
	 * 
	 * @return 数据定义接口
	 */
	public DbDefiner getDbDefiner() {
		ConnectionFactory connfactory = DefaultConnectionFactory.get(dsname, true);
		
		return connfactory.getDbDefiner();
	}

	/**
	 * 获得一个数据库特征功能的对象
	 * 
	 * @return
	 */
	public Dialect getDialect() {
		ConnectionFactory connfactory = DefaultConnectionFactory.get(dsname, true);
		
		return connfactory.getDialect();
	}

	/**
	 * 是否在一个事务中，true表示在一个事务中，false表示不在一个事务中
	 * 
	 * @return 是否在一个事务中
	 */
	public boolean isTransaction() {
		return isTransaction;
	}

	/**
	 * 获得实体对象管理器
	 * 
	 * @return 实体对象管理器
	 */
	public EntityInfoManager getEntityManager() {
		return entityManager;
	}

	/**
	 * 构造方法
	 * 
	 * @param connfactory 数据连接或数据定义
	 */
	public SessionImpl(String dsname, EntityInfoManager entityManager) {
		this.dsname = dsname;
		this.entityManager = entityManager;
	}

	/**
	 * 获得一个数据库连接
	 * 
	 * @return 数据库连接
	 */
	public Connection getConnection() {
		boolean needNewConn = false;

		ConnectionFactory connfactory = DefaultConnectionFactory.get(dsname, true);
		
		try {
			if (conn == null || conn.isClosed() || !conn.isActive()) {
				needNewConn = true;
			} else {
				//检查连接是否真实可用
				String checkSql = connfactory.getDbType().getCheckSQL();
				
				if (StrFunc.isNull(checkSql)) {
					return conn;
				}
				
				try {
					Statement stmt = conn.createStatement();
					try {
						stmt.execute(checkSql);
					} finally {
						stmt.close();
					}
				} catch (SQLException e) {
					needNewConn = true;
					/*
					 * 该连接不可用时，关闭该连接，即放回空闲连接池。
					 * 其他特殊处理此处无需完成，在重新获取连接时  DataSource 会主动清除失效的连接。
					 */
					conn.realClose();
				}
			}
		} catch (SQLException e) {
			needNewConn = true;
		}
		
		if (needNewConn) {
			try {
				conn = new ORMConnection(connfactory.getConnection());
	
				if (isTransaction && conn.getAutoCommit()) {
					conn.setAutoCommit(false);
				}
			} catch (SQLException e) {
				conn = null;
				throw new ORMSQLException("com.esen.jdbc.orm.impl.sessionimpl.2","获取连接失败", e);
			}
		}
		
		return conn;
	}
	
	/**
	 * 根据给定的实体名获得一个实体对象
	 * 
	 * @param entityName 实体名
	 * @return 实体对象
	 */
	private EntityInfo getEntity(String entityName) {
		EntityInfo entity = this.entityManager.getEntity(entityName);
		if (entity == null) {
			throw new ORMException("com.esen.jdbc.orm.impl.sessionimpl.3","无法获取实体对象 {0}",new Object[]{entityName});
		}
		return entity;
	}

	/**
	 * {@inheritDoc}   
	 */
	public <T> Query<T> createQuery(Class<T> clazz, String entityName) {
		EntityInfo entity = getEntity(entityName);
		return new QueryProxy(new QueryExecuter(this, entity));
	}
	
	/**
	 * {@inheritDoc}   
	 */
	public <T> Executer<T> createExecuter(Class<T> clazz, String entityName) {
		EntityInfo entity = getEntity(entityName);
		return new ExecuterProxy(new ExecuterImpl<T>(this, entity));
	}

	/**
	 * {@inheritDoc}   
	 */
	public <T> Batch<T> createUpdateBatch(Class<T> clazz, String entityName, String... properties) {
		EntityInfo entity = getEntity(entityName);
		return new BatchImpl<T>(this, entity, Batch.BATCHTYPE_UPDATE, properties);
	}

	/**
	 * {@inheritDoc}   
	 */
	public <T> Batch<T> createInsertBatch(Class<T> clazz, String entityName, String... properties) {
		EntityInfo entity = getEntity(entityName);
		return new BatchImpl<T>(this, entity, Batch.BATCHTYPE_INSERT, properties);
	}
	
	/**
	 * {@inheritDoc}   
	 * 
	 * 注意：
	 * properties 中不要出现属性值可能为 null 的属性，否则执行结果会与预期不相符
	 * 推荐使用主键
	 * 
	 * properties 为 null 时，表示所有属性的值都必须和指定的对象中的属性值相同
	 */
	public <T> Batch<T> createDeleteBatch(Class<T> clazz, String entityName, String... properties) {
		EntityInfo entity = getEntity(entityName);
		return new BatchImpl<T>(this, entity, Batch.BATCHTYPE_DELETE, properties);
	}

	/**
	 * {@inheritDoc}   
	 */
	public <T> Update<T> createUpdate(Class<T> clazz, String entityName) {
		EntityInfo entity = getEntity(entityName);
		return new UpdateProxy(new UpdateExecuter<T>(this, entity));
	}

	/**
	 * {@inheritDoc}   
	 */
	public int delete(String entityName, Expression condition, Object... params) {
		EntityInfo entity = getEntity(entityName);
		Executer exe = createExecuter(entity.getClass(), entityName);
		return exe.delete(condition, params);
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean exists(String entityName, Object idValue) {
		EntityInfo entityInfo = getEntityInfo(entityName);
		if (entityInfo == null) {
			return false;
		}
		
		Query executer = createQuery(entityInfo.getClass(), entityName);
		return executer.exist(idValue);
	}

	/**
	 * {@inheritDoc}   
	 */
	public void add(String entityName, Object object) {
		EntityInfo entity = getEntityInfo(entityName);
		
		Executer exe = createExecuter(entity.getClass(), entityName);
		exe.add(object);
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(String entityName, Object object) {
		EntityInfo entityInfo = getEntityInfo(entityName);
		DynaBean<?> bean = DynaBean.getDynaBean(object);
		Object oldIdValue = ORMUtil.getPrimaryKeyValue(bean, entityInfo);
		update(entityName, oldIdValue, object);
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(String entityName, Object oldId, Object object) {
		EntityInfo entity = getEntityInfo(entityName);
		
		Executer exe = createExecuter(entity.getClass(), entityName);
		exe.update(oldId, object);
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(String entityName, Object object, String... properties) {
		EntityInfo entity = getEntityInfo(entityName);
		DynaBean<?> bean = DynaBean.getDynaBean(object);
		Object oldId = ORMUtil.getPrimaryKeyValue(bean, entity);
		
		Executer exe = createExecuter(entity.getClass(), entityName);
		exe.update(oldId, object, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(String entityName, Object oldId, Object object, String... properties) {
		EntityInfo entity = getEntityInfo(entityName);
		
		Executer updateExe = createExecuter(entity.getClass(), entityName);
		updateExe.update(oldId, object, properties);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean delete(String entityName, Object idValue) {
		EntityInfo entity = getEntityInfo(entityName);
		
		Executer updateExe = createExecuter(entity.getClass(), entityName);
		int rows = updateExe.delete(idValue);
		return rows > 0 ? true : false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void beginTransaction() {
		if (isTransaction) {
			return;
		}
		
		if (conn == null) {
			conn = (ORMConnection) getConnection();
		}
		
		try {
			if (conn.getAutoCommit()) {
				isTransaction = true;
				conn.setAutoCommit(false);
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void commit() {
		try {
			if (!conn.getAutoCommit()) {
				conn.commit();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void rollback() {
		try {
			if (!conn.getAutoCommit()) {
				conn.rollback();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void close() {
		try {
			if (conn != null && !conn.isClosed()) {
				conn.realClose();
				conn = null;
			}
		} catch (SQLException e) {
			throw new ORMSQLException("com.esen.jdbc.orm.impl.sessionimpl.4","关闭jdbc连接出错!", e);
		}
	}

	/**
	 * 检查参数有效性，获取实体对象
	 * @param entityName
	 * @return
	 * @throws SQLException
	 */
	public EntityInfo getEntityInfo(String entityName) {
		//TODO 国际化
		if (StrFunc.isNull(entityName)) {
			throw new ORMException("JDBC.COMMON.SCANNOTBEEMPTY","参数 {0} 不能为空！",new Object[]{"entityName"});
		}

		if (entityManager != null) {
			EntityInfo entityInfo = entityManager.getEntity(entityName);
			if (entityInfo == null) {
				throw new ORMException("com.esen.jdbc.orm.impl.sessionimpl.6","实体不存在");
			}

			return entityInfo;
		}

		return null;
	}

	public boolean delete(String entityName) {
		return delete(entityName, null);
	}
	
	/**
	 * 获取连接池的名字
	 * 
	 * @return 连接池的名字
	 */
	public String getConnName() {
		return dsname;
	}
	
	/**
	 * 获取该会话是否是自动提交的
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public boolean getAutoCommit() throws SQLException {
		return getConnection().getAutoCommit();
	}
	
	/**
	 * 设置该会话是否自动提交
	 * 
	 * @param autoCommit
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		getConnection().setAutoCommit(autoCommit);
	}
}