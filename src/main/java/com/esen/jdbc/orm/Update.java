package com.esen.jdbc.orm;

import com.esen.util.exp.Expression;

/**
 * 执行一段update语句sql的对象，update语句包含3段类型
 * 1. 执行SQL的where条件，对应方法：setCondition();
 * 2. update语句中的字段及对应的值，对应方法：setPropertyValue()、setPropertyExp()
 * 3. 调用1，2两步操作后，提交动作，对应方法executeUpdate()
 * @param <T>
 *
 * @author wang
 */
public interface Update<T> {
	/**
	 * @param conditionExp 条件表达式，left(userid,2)=? and upid=?
	 * @param params 条件表达式中变量对应的值，如果条件中没有?，该参数可以为null
	 * @return 当前的Update对象
	 */
	Update setCondition(Expression conditionExp, Object... params);

	/**
	 * 设置一个字段的值
	 * @param property 对象的属性名(非数据字段)
	 * @param value  更新的属性值
	 * @return  当前的Update对象
	 */
	Update setPropertyValue(String property, Object value);    // extField = '4'

	/**
	 * 设置一个属性的值
	 * @param property 对象的属性名(非数据字段)
	 * @param exp 表达式对象
	 * @return  当前的Update对象
	 */
	Update setPropertyExp(String property, Expression exp); // id = left(name,2)

	/**
	 * 执行update提交
	 * @return 当前SQL执行后更新的条目数
	 */
	int executeUpdate();

}
