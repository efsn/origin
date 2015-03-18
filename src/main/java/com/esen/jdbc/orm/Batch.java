package com.esen.jdbc.orm;

/**
 * 执行批量操作的Query,类似与jdbc中的PreparedStatement接口，执行批量SQL
 * 示例：
 *  User[] users = ...
 *  BatchQuery<User> query = ...
 *  for(User u:users){
 *  	 query.addBatch(u);
 *  }
 *  query.exectue();
 **********************************************************
 * 执行批量操作的Batch,类似与jdbc中的PreparedStatement接口，执行批量SQL
 * 示例：
 *  User[] users = ...
 *  Batch<User> batch = ...
 *  for(User u:users){
 *  	 batch.addBatch(u);
 *  }
 *  batch.exectue();
 *  这个类只能执行批量更新么？
 * 
 * @author wang
 */
public interface Batch<T> {

	final static int BATCHTYPE_INSERT = 1;
	final static int BATCHTYPE_UPDATE = 2;
	final static int BATCHTYPE_DELETE = 3;

	/**
	 * 批量提交一个待更新的对象
	 * @param object
	 */
	void addBatch(T object);

	/**
	 * 批量操作后的提交动作
	 */
	void exectue();
	
	/**
	 * 操作完成后关闭
	 */
	void close();
}
