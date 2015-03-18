package com.esen.jdbc.orm;

/**
 * QueryResult的迭代器对象
 * @param <T> javabean对象
 *
 * @author wang
 */
public interface Iterator<T> {

	/**
	 * @return 获取结果集中下一个对象，如果next指针结束，则返回空
	 */
	T next();

	/**
	 * 关闭当前的迭代器
	 */
	void close();

	/**
	 * @return 迭代器是否还有下一个对象
	 */
	boolean hasNext();
}
