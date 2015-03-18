package com.esen.jdbc.orm;

import java.io.Serializable;

import com.esen.jdbc.dialect.DbDefiner;

/**
 * 实体对象的一个属性，属性的name与javabean的定义的属性是一对一的关系
 *
 * @author wang
 */
public interface Property extends Serializable {

	/**
	 * 整型
	 */
	public static final char FIELD_TYPE_INT = DbDefiner.FIELD_TYPE_INT;

	/**
	 * 字符串类型
	 */
	public static final char FIELD_TYPE_STR = DbDefiner.FIELD_TYPE_STR;

	/**
	 * 日期+时间格式
	 */
	public static final char FIELD_TYPE_DATE = DbDefiner.FIELD_TYPE_TIMESTAMP;

	/**
	 * 字符型大字段
	 */
	public static final char FIELD_TYPE_CLOB = DbDefiner.FIELD_TYPE_CLOB;

	/**
	 * 数值型
	 */
	public static final char FIELD_TYPE_FLOAT =DbDefiner.FIELD_TYPE_FLOAT;

	/**
	 * 逻辑型
	 */
	public static final char FIELD_TYPE_LOGIC = DbDefiner.FIELD_TYPE_LOGIC;

	/**
	 * 二进制类型
	 */
	public static final char FIELD_TYPE_BINARY =DbDefiner.FIELD_TYPE_BINARY;

	/**
	 * @return 获取属性名字
	 */
	String getName();

	/**
	 * @return 获取属性的标题，一般是中文名称
	 */
	String getCaption();

	/**
	 * @return 该属性对应的字段名
	 */
	String getFieldName();

	/**
	 * @return 获取字段类型
	 */
	char getType();

	/**
	 * @return 获取属性字段的长度
	 */
	int length();

	/**
	 * @return 获取属性字段的标度
	 */
	int getScale();

	/**
	 * @return 是否允许为空
	 */
	boolean isNullable();

	/**
	 * @return 是否唯一
	 */
	boolean isUnique();

	/**
	 * 是否为主键
	 * @return
	 */
	boolean isPrimaryKey();

	/**
	 * @return 是否为自增长字段
	 */
	boolean isAutoInc();

}
