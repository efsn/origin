package com.esen.jdbc.orm;

/**
 * 
 * @author liujin
 *
 */
public class BeanBuilderBean implements BeanBuilder {
	/**
	 * 创建对象的 javabean
	 */
	private Class bean;
	
	/**
	 * 创建对象的 javabean 中创建对象的方法名
	 */
	private String method;
	
	/**
	 * 创建对象的 javabean 中创建对象的方法的参数名
	 */	
	private String[] arguments;
	
	/**
	 * {@inheritDoc}
	 */
	public Class getBuilder() {
		return bean;
	}
	
	/**
	 * 设置 Builder 类
	 * @param bean
	 */
	public void setBuilder(Class bean) {
		this.bean = bean;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMethod() {
		return method;
	}
	
	/**
	 * 设置方法名
	 * 
	 * @param method
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getArguments() {
		return arguments;
	}
	
	/**
	 * 设置参数的属性名
	 * 
	 * @param arguments
	 */
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

}
