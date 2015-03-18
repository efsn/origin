package com.esen.jdbc.orm;

import java.sql.Connection;

import com.esen.util.exp.Expression;

/**
 * 会话
 * 
 * @author wang
 */
public interface Session {
	
	/**
	 * @return 对应的connection对象，在混合使多种数据库访问框架时会用到
	 */
	Connection getConnection();

	/**
	 * 根据实体名称，创建一个Query对象
	 * 
	 * @param <T>
	 * 
	 * @param entityName
	 *            实体名称
	 * @return Query对象
	 */
	<T> Query<T> createQuery(Class<T> clazz, String entityName);

	/**
	 * 根据实体名称，创建一个Executer对象
	 * 
	 * @param <T>
	 * 
	 * @param entityName
	 *            实体名称
	 * @return Query对象
	 */
	<T> Executer<T> createExecuter(Class<T> clazz, String entityName);

	/**
	 * 根据entityName创建一个Batch对象
	 * 
	 * @param clazz 实体的 class
	 * @param entityName 实体名
	 * @param properties 需要更新的属性
	 * @return Batch
	 */
	<T> Batch<T> createUpdateBatch(Class<T> clazz, String entityName,
			String... properties);

	/**
	 * 根据entityName创建一个Batch对象
	 * 
	 * @param clazz 实体的 class
	 * @param entityName 实体名
	 * @param properties 需要插入数据的属性
	 * @return Batch
	 */
	<T> Batch<T> createInsertBatch(Class<T> clazz, String entityName,
			String... properties);
	
	/**
	 * 根据entityName创建一个Batch对象
	 * 
	 * @param clazz 实体的 class
	 * @param entityName 实体名
	 * @param properties 需要删除的对象的满足条件的属性
	 * @return
	 */
	<T> Batch<T> createDeleteBatch(Class<T> clazz, String entityName,
			String... properties);

	/**
	 * 根据entityName创建一个Update对象
	 * 
	 * @param entityName
	 *            实体名称
	 * @return Update对象
	 */
	<T> Update<T> createUpdate(Class<T> clazz, String entityName);

	/**
	 * 按条件删除
	 * 
	 * @param condition
	 * @param params
	 * @return
	 */
	int delete(String entityName, Expression condition, Object... params);

	/**
	 * 判断一个PK为idValue的对象是否存在
	 * 
	 * @param idValue
	 *            pk值
	 * @return 是否存在
	 */
	boolean exists(String entityName, Object idValue);

	/**
	 * 向数据库里面插入一条对象记录，如果存在相同的PK记录，抛出异常
	 * 
	 * @param object
	 *            javabean<T>对象
	 * @throws SQLException 
	 */
	void add(String entityName, Object object);

	/**
	 * 更新一个对象，如果不存在相同的PK记录，抛出异常
	 * 
	 * @param object
	 *            javabean<T>对象
	 */
	void update(String entityName, Object object);

	/**
	 * 更新一个对象，如果不存在相同的PK记录，抛出异常
	 * 
	 * @param object
	 *            javabean<T>对象
	 * @param oldId
	 *            原id
	 */
	void update(String entityName, Object oldId, Object object);

	/**
	 * 更新javabean<T>部分属性
	 * 
	 * @param object
	 *            javabean<T>对象
	 * @param oldId
	 *            原id
	 * @param properties
	 *            属性列表
	 */
	void update(String entityName, Object object, String... properties);

	/**
	 * 更新javabean<T>部分属性
	 * 
	 * @param object
	 *            javabean<T>对象
	 * 
	 * @param oldId
	 *            原id
	 * 
	 * @param properties
	 *            属性列表
	 */
	void update(String entityName, Object oldId, Object object,
			String... properties);

	/**
	 * 从数据库中删除一条记录
	 * 
	 * @param idValue
	 * @return
	 */
	boolean delete(String entityName, Object idValue);

	/**
	 * 从数据库表中删除全部记录
	 */
	boolean delete(String entityName);
	
	/**
	 * 开始一段事务,一个事务可以做多次修改、查询操作， Session session = ... User user = ...
	 * session.beginTransaction(); try{ Query query = session.createQuery;
	 * query.update(user); session.commit(); catch(Exception e){
	 * session.roolback(); }
	 * @throws SQLException 
	 */
	void beginTransaction();

	/**
	 * 提交事务
	 */
	void commit();

	/**
	 * 事务回滚
	 */
	void rollback();

	/**
	 * 关闭当前的session
	 */
	void close();

}
