package com.esen.jdbc.orm;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * 定义一个实体对象
 *
 * @author wang
 */
public interface EntityInfo<T> extends Serializable{

	/**
	 * 获取实体名
	 * 
	 * @return 实体名
	 */
	String getEntityName();

	/**
	 * 获取实体对应的数据库表名
	 * 
	 * @return 数据库表名
	 */
	String getTable();

	/**
	 * 获取实体的主键属性
	 * 
	 * @return 主键属性
	 */
	Property getPrimaryKey();

	/**
	 * 获取实体对象对应的javabean对象
	 * 
	 * @return javabean对象
	 */
	Class<T> getBean();
	
	/**
	 * 获取实体对象对应的提供创建对象的类和方法
	 * 
	 * @return BeanBuilder 对象
	 */
	BeanBuilder getBeanBuilder();

	/**
	 * 根据"属性名"获取属性对象
	 * 
	 * @param propertyName 属性名
	 * @return 属性对象
	 */
	Property getProperty(String propertyName);

	/**
	 * 根据"属性名"获取属性对象，不区分大小写
	 * 
	 * @param propertyName 属性名
	 * @return 属性对象
	 */
	Property getPropertyIgoreCase(String propertyName);
	
	/**
	 * 根据属性对应的"字段名"获取属性
	 * 
	 * @param fieldName 字段名
	 * @return 属性对象
	 */
	Property getPropertyByField(String fieldName);

	/**
	 * 获取所有的属性对象
	 * 
	 * @return 属性
	 */
	List<Property> getProperties();
	
	/**
	 * 获取所有的索引
	 * 
	 * @return 索引
	 */
	List<Index> listIndexes();
	
	/**
	 * 将实体对象以 mapping.xml 输出到流对象中
	 * 
	 * @param out 流对象
	 */
	void saveTo(OutputStream out);
	
	/**
	 * 获取当前实体对象缓存策略
	 * 
	 * @return 缓存策略
	 */
	CachePolicy getCachePolicy();
}
