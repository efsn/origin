package com.esen.jdbc.orm;

/** 
 * 
 * <beanBuilder bean="com.esen.dataquality.rule.RuleBean">
 * 
 *a.RuleBean b=  RuleBean newInstance()
 *  RuleBean b=  RuleBuilder.creaete() 
 * 
 *   b. RuleBean.setP1("111");
 * 	<beanBuilder bean="com.esen.dataquality.rule.RuleBuilder" mothod="create" >
 * 
 *
			<argument properties="type" />			
	</beanBuilder>
 *
 * @Date  : 2014-4-18
 * @Author: wang(rokr@qq.com)
 * 
 */
public interface BeanBuilder {

	/**
	 * @return 获取Bean的builder工厂类
	 */
	Class getBuilder();

	/**
	 * @return 创建Bean的方法
	 */
	String getMethod();

	/**
	 * @return 参数,对应为property的name
	 */
	String[] getArguments();

}
