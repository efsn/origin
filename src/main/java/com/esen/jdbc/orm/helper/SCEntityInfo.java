package com.esen.jdbc.orm.helper;

import com.esen.jdbc.orm.EntityInfo;

public interface SCEntityInfo extends EntityInfo {

	/**
	 * @return 数据开始有效期属性名
	 */
	String getFromDatePropertyName();

	/**
	 * @return 数据结束有效期属性名
	 */
	String getToDatePropertyName();

	/**
	 * id 属性名
	 */
	String getIdPropertyName();
}
