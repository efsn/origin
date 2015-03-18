package com.esen.jdbc.orm;

import com.esen.util.exp.Expression;

public interface Executer<T> {
	
	/**
	 * 根据给定的表达式和参数值删除
	 * 
	 * @param condition 条件表达式
	 * @param params 参数值
	 * @return 删除的记录数
	 */
	public int delete(Expression condition, Object[] params);
	
	/**
	 * 根据给定的 ID 删除
	 * 
	 * @param idValue 指定的id
	 * @return 删除的记录数
	 */
	public int delete(Object idValue);
	
	/**
	 * 向数据库插入一条记录
	 * 
	 * @param object
	 */
	public void add(Object object);
	
	/**
	 * 根据指定的 id 更新部分属性
	 * 
	 * @param oldId 指定的 ID
	 * @param object 新的对象
	 * @param properties 需要更新的属性
	 */
	public void update(Object oldId, Object object, String...properties);

}
