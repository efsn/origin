package com.esen.jdbc.orm;

import java.io.InputStream;
import java.util.Set;

/**
 * 实体对象的管理
 *
 * @author wang
 */
public interface EntityInfoManager {

	/**
	 * @return 获取管理器中实体对象的个数
	 */
	int size();

	/**
	 * 通过实体对象的entityName获取实体对象
	 * @param entityName
	 * @return
	 */
	<T> EntityInfo<T> getEntity(String entityName);

	/**
	 * 根据 javabean 获取对应的实体对象的集合，一个 javabean 与  Entity 是“一对多”的关系
	 * @param cls
	 * @return
	 */
	Set<EntityInfo> getEntityByClass(Class<?> cls);

	/**
	 * @return 获取所有实体对象的ID集合
	 */
	Set<String> getEntityNames();

	/**
	 * 从一个流对象中加载一个Entity对象
	 * @param in xml文件流
	 * @return
	 */
	<T> EntityInfo<T> loadEntityFrom(InputStream in);

	/**
	 * 增加一个实体到EntityManager对象中
	 * @param entity
	 */
	void addEntity(EntityInfo entity);

	/**
	 * 根据实体名字移除Entity
	 * @param entityName 实体名字
	 * @return 移除的实体对象
	 */
	EntityInfo remove(String entityName);
}
