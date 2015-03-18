package com.esen.jdbc.orm.helper;

import com.esen.jdbc.orm.QueryResult;

/**
 * @param <T> 缓慢变化对象，必须包含“开始时间”和“结束时间”两个字段
 *
 * @author wang
 */
public interface SCHelper<T> {

	/**
	 * 根据指定的时间获取对象
	 * @param idValue
	 * @param cal
	 * @return
	 */
	T get(Object idValue, long date);

	/**
	 * 根据指定的对象 ID 获取所有时间点的数据
	 * @param idValue
	 * @return
	 */
	QueryResult<T> get(Object idValue);
	
	/**
	 * 根据指定的时间获取对象列表
	 * @param idValue
	 * @param cal
	 * @return
	 */
	QueryResult<T> getAll(long date);

	/**
	 * 插入一条记录
	 * 
	 * @param T
	 */
	void add(Object T);

	/**
	 * 更新一条记录，可以修改其“开始时间”和“结束时间”
	 * @param T
	 * @return
	 */
	void update(Object T);

	/**
	 * 可以修改对象ID,历史数据的“结束时间”=修改后"开始时间"
	 * @param oldIdValue
	 * @param Tux
	 * @return
	 */
	void update(Object oldIdValue, Object T);

	/**
	 * 删除一条记录，可以修改其“开始时间”和“结束时间”
	 * @param T
	 * @param strechUp 如果为true,删除时优先扩展左边相邻节点时间区间，否则优先扩展右边节点时间区间
	 * @return
	 */
	void delete(Object idValue, boolean stretchUp);
}
