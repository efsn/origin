package com.esen.jdbc.orm.helper;

import java.util.List;

import com.esen.jdbc.orm.Iterator;
import com.esen.jdbc.orm.QueryResult;

public interface TreeQueryResult<T> extends QueryResult {
	/**
	 * 分页查询结果,页码从0开始计算，pageIndex传-1则查询所有。
	 * @param pageIndex 当前页码,备注：一般来说,index都是从0开始，为避免有人第一页传0,有人传1,因此将参数名从pageNum修改为pageIndex
	 * @param pageSize 每页显示的条目
	 * @return
	 */
	public List<TreeObj<T>> list(int pageIndex, int pageSize);

	/**
	 * 以迭起器的形式返回结果集
	 * @param pageIndex 当前页码
	 * @param pageSize 每页显示的条目
	 * @return
	 */
	public Iterator<TreeObj<T>> iterator(int pageIndex, int pageSize);

}
