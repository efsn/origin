package com.esen.jdbc.orm.helper;

public interface TreeObj<T> {
	/**
	 * 直接下级个数
	 * @return
	 */
	public int getChildCount();

	/**
	 * @return 获取对象
	 */
	public T getObject();
}
