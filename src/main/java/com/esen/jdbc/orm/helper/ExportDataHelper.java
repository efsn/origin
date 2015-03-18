package com.esen.jdbc.orm.helper;

import com.esen.util.exp.Expression;

public class ExportDataHelper {
	/**
	 * 导出的数据的属性名，为空时表示所有属性
	 */
	private String[] propertyNames;
	
	/**
	 * 导出的数据需要满足的条件条件表达式
	 */
	private Expression exp;
	
	/**
	 * 排序条件
	 */
	private String orderbyProperties;
	
	/**
	 * 表达式中的参数值
	 */
	private Object[] params;
	
	/**
	 * 数据分隔符，默认为制表符
	 */
	private char separator = '\t';
	
	/**
	 * 是否显示属性的描述信息
	 */
	private boolean showCaption = false;
			
	/**
	 * 构造方法
	 */
	public ExportDataHelper() {
		
	}

	/**
	 * 设置需要导出的数据的属性
	 * 
	 * @param propertyNames
	 */
	public void setPropertyNames(String[] propertyNames) {
		this.propertyNames = propertyNames;
	}
	
	/**
	 * 获取需要导出的数据的属性名
	 * 
	 * @return 属性名
	 */
	public String[] getPropertyNames() {
		return this.propertyNames;
	}

	/**
	 * 获取导出数据的条件表达式
	 * 
	 * @return 条件表达式
	 */
	public Expression getExp() {
		return exp;
	}

	/**
	 * 设置导出数据的条件表达式
	 * 
	 * @param exp 条件表达式
	 */
	public void setExp(Expression exp) {
		this.exp = exp;
	}

	/**
	 * 获取导出数据的排序条件
	 * 
	 * @return 排序条件
	 */
	public String getOrderbyProperties() {
		return orderbyProperties;
	}

	/**
	 * 设置导出数据的排序条件
	 * 
	 * @param orderbyProperties 排序条件
	 */
	public void setOrderbyProperties(String orderbyProperties) {
		this.orderbyProperties = orderbyProperties;
	}

	/**
	 * 获取表达式中的参数值
	 * 
	 * @return 表达式中的参数值
	 */
	public Object[] getParams() {
		return params;
	}

	/**
	 * 设置表达式中的参数值
	 * 
	 * @param params 表达式中的参数值
	 */
	public void setParams(Object[] params) {
		this.params = params;
	}

	/**
	 * 获取导出数据的分隔符
	 * 
	 * @return 分隔符
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * 设置导出数据的分隔符
	 * 
	 * @param separator 分隔符
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	/**
	 * 是否显示属性的标题信息
	 * 
	 * @return 是否显示属性的标题信息
	 */
	public boolean isShowCaption() {
		return showCaption;
	}

	/**
	 * 设置是否显示属性的描述信息
	 * 
	 * @param showCaption 是否显示属性的描述信息
	 */
	public void setShowCaption(boolean showCaption) {
		this.showCaption = showCaption;
	}
}
