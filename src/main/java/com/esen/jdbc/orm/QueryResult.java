package com.esen.jdbc.orm;

import java.util.List;

/**
 * QuerySession查询出来的结果，可以实现分页，查询总条目数
 * @param <T> ORM的javabean实体对象
 *
 * @author wang
 */
public interface QueryResult<T> {

	/**
	 * @return 计算结果的总条目数
	 */
	public int calcTotalCount();

	/**
	 * 分页查询结果,页码从0开始计算，pageIndex传-1则查询所有。
	 * @param pageIndex 当前页码。备注：一般来说，index都是从0开始，为避免有人第一页传0，有人传1,因此将参数名从pageNum修改为pageIndex
	 * @param pageSize 每页显示的条目
	 * @return
	 */
	public List<T> list(int pageIndex, int pageSize);

	/**
	 * 以迭代器的形式返回分页查询的结果集
	 * @param pageIndex 当前页码
	 * @param pageSize 每页显示的条目
	 * @return
	 */
	public Iterator<T> iterator(int pageIndex, int pageSize);

}
