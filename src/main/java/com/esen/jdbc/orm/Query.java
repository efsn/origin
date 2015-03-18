package com.esen.jdbc.orm;

import com.esen.util.exp.Expression;

/**
 * 一个实体中执行查询、删除、更新操作的接口对象
 * @param <T> 泛型javabean<T>对象,每个对象都必须在定义唯一PK属性
 *
 * @author wang
 */
public interface Query<T> {

	/**
	 * 根据ID查询一个实体对象
	 * @param idValue pk值
	 * @return javabean<T>对象
	 */
	T get(Object idValue);

	/**
	 * 根据ID查询一个加载指定属性的<T>对象
	 * @param idValue pk值
	 * @param perperties 指定的属性名
	 * @return 返回javabean<T>对象
	 */
	T get(Object idValue, String... propertyNames);

	/**
	 * 根据指定条件查询一个加载指定属性的实体对象
	 * 
	 * left(name,2)='wuhan' and right('name',2)=? and id like ?
	 * @param condition 查询条件，支持表达式，“?”为变量参数，字段名为实体，对象名字.属性名
	 * 1、left(userid,2)=? and createDate<today()
	 * @param params 变量参数
	 * @return 返回QueryResult集合对象
	 */
	QueryResult<T> query(Expression condition, String[] propertyNames, Object... params);

	/**
	 * 带排序字段的查询
	 * left(name,2)='wuhan' and right('name',2)=? and id like ?
	 * @param condition 查询条件，支持表达式，“?”为变量参数，字段名为实体，对象名字.属性名
	 * 1、left(userid,2)=? and createDate<today()
	 * @param orderbyProperties 排序字段的键值对,key为property，value是排序方式：降序:true、升序:false,每组键值对用逗号分割
	 *        id=true,name=false,aaa=true
	 * @param params 变量参数
	 * @return 返回QueryResult集合对象
	 */
	QueryResult<T> query(Expression condition, String orderbyProperties, String[] propertyNames, Object... params);

	/**
	 * 查询出数据库所有的记录
	 * @return 返回QueryResult集合对象
	 */
	QueryResult<T> list();

	/**
	 * 是否存在该 id 的记录
	 * @param idValue
	 * @return
	 */
	boolean exist(Object idValue);

}
