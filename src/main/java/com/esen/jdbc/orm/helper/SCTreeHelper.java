package com.esen.jdbc.orm.helper;

import com.esen.jdbc.orm.QueryResult;

/**
 * @param <T> 缓慢变化对象，必须包含“开始时间”和“结束时间”两个字段
 *
 * @author wang
 */
public interface SCTreeHelper<T> extends SCHelper {
	
	/**
	 * 获取根节点对象
	 * 
	 * @return
	 */
	QueryResult<T> getRoots(long date);

	/**
	 * 列出孩子节点
	 * 
	 * @param idValue 节点id
	 * @param recursive 是否递归
	 * @param date 时间
	 * @return
	 */
	QueryResult<T> listChildren(Object idValue, boolean recursive, long date);

	/**
	 * 列出孩子节点
	 * 
	 * @param idValue 节点 id
	 * @param recursive 是否递归， true 表示递归， false 表示不递归
	 * @param date 时间
	 * @param orderByProperties 排序属性
	 * @return
	 */
	QueryResult<T> listChildren(Object idValue, boolean recursive, long date, String orderByProperties);

	/**
	 * 删除节点
	 * 
	 * @param idValue
	 */
	void delete(Object idValue);
}
