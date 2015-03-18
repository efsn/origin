package com.esen.jdbc.orm.helper;

import com.esen.jdbc.orm.QueryResult;

/**
 * 树形操作控件
 * 
 * @param <T>
 * 
 * @author wang
 */
public interface TreeHelper<T> {

	/**
	 * 获取根节点对象
	 * 
	 * @return
	 */
	QueryResult<T> getRoots();

	/**
	 * 列出孩子节点
	 * 
	 * @param id ID
	 * @param recursive 是否递归
	 * @return
	 */
	QueryResult<T> listChildren(Object idValue, boolean recursive);

	/**
	 * 列出孩子节点
	 * 
	 * @param id ID
	 * @param recursive 是否递归
	 * @param orderByProperties 排序属性
	 * @return
	 */
	QueryResult<T> listChildren(Object idValue, boolean recursive, String orderByProperties);

	/**
	 * 更新树形节点数据， 允许修改节点ID,如果节点ID发生变化，除了更新当前节点信息意外，下级节点的upids也将随之做出相应的调整
	 * 如果是“非叶子节点”，除了更新当前节点信息以外，下级节点的upids也将随之做出相应的调整
	 * 
	 * @param idValue
	 *            待更新的节点ID
	 * @param object
	 *            更新后的数据对象
	 */
	void update(Object idValue, T object);

	/**
	 * 更新指定 id 节点的指定属性的数据，可以修改 id 值
	 * @param idValue
	 * @param object
	 * @param properties
	 */
	void update(Object idValue, T object, String... properties);

	/**
	 * 删除节点，只允许删除叶子节点
	 * 
	 * @param idValue
	 */
	void delete(Object idValue);
	
	/**
	 * 删除节点
	 * 
	 * @param idValue 节点 id
	 * @param recursive 是否递归删除，
	 * 									true表示递归删除，即删除当前结点和其子节点
	 * 									false表示只删除当前节点，即只能删除叶子节点
	 */
	void delete(Object idValue, boolean recursive);
	
	/**
	 * 增加节点
	 * @param object 节点对象
	 */
	void add(T object);
}
