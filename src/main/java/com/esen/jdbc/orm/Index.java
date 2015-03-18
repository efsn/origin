package com.esen.jdbc.orm;

/**
 * 
 * 实体的索引对象
 *
 * @author wang
 */
public interface Index {

	/**
	 * @return 获取索引名字
	 */
	String getIndexName();

	/**
	 * @return 获取索引的字段名，多个字段用逗号分割
	 */
	String getIndexFields();

	/**
	 * @return 是否唯一
	 */
	boolean isUnique();

}
