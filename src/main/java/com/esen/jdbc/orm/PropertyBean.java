package com.esen.jdbc.orm;

/**
 * 实体对象的一个属性，属性的 name 与 javabean 的定义的属性是一对一的关系
 *
 * @author wangshzh
 */
public class PropertyBean implements Property {

	/**
	 * 属性名字
	 */
	private String name;

	/**
	 * 该属性对应的字段名
	 */
	private String fieldName;

	/**
	 * 字段类型
	 */
	private char type = 0;

	/**
	 * 属性字段的长度
	 */
	private int length;

	/**
	 * 字段的精度
	 */
	private int scale;

	/**
	 * 是否允许为空
	 */
	private boolean nullable = true;

	/**
	 * 是否唯一
	 */
	private boolean unique = false;

	/**
	 * 是否为主键
	 */
	private boolean primaryKey = false;

	/**
	 * 是否为自增长字段，目前一张表只支持一个自增长字段
	 */
	private boolean autoInc = false;

	/**
	 * 中文名称
	 */
	private String caption;

	/**  
	 * 设置自增长  
	 * @param autoInc 自增长  
	 */
	public void setAutoInc(boolean autoInc) {
		this.autoInc = autoInc;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getCaption() {
		return this.caption;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getFieldName() {
		return this.fieldName;
	}

	/**
	 * {@inheritDoc}   
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}   
	 */
	public int getScale() {
		return this.scale;
	}

	/**
	 * {@inheritDoc}   
	 */
	public char getType() {
		return this.type;
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean isNullable() {
		return this.nullable;
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean isPrimaryKey() {
		return this.primaryKey;
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean isUnique() {
		return this.unique;
	}

	/**
	 * {@inheritDoc}   
	 */
	public int length() {
		return this.length;
	}

	/**
	 * @param caption 设置caption
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**  
	 * 设置属性对应的字段名  
	 * @param fieldName 属性对应的字段名  
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**  
	 * 设置属性字段的长度  
	 * @param length 属性字段的长度  
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**  
	 * 设置是否允许为空  
	 * @param nullable 是否允许为空  
	 */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	/**  
	 * 设置是否为主键  
	 * @param primaryKey 是否为主键  
	 */
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**  
	 * 设置字段的精度  
	 * @param scale 字段的精度  
	 */
	public void setScale(int scale) {
		this.scale = scale;
	}

	/**  
	 * 设置字段类型  
	 * @param type 字段类型  
	 */
	public void setType(char type) {
		if (type != Property.FIELD_TYPE_INT
				&& type != Property.FIELD_TYPE_LOGIC
				&& type != Property.FIELD_TYPE_STR
				&& type != Property.FIELD_TYPE_FLOAT
				&& type != Property.FIELD_TYPE_DATE
				&& type != Property.FIELD_TYPE_CLOB
				&& type != Property.FIELD_TYPE_BINARY) {
			throw new ORMException("com.esen.jdbc.orm.propertybean.1","数据类型定义错误，不支持{0} ",new Object[]{type});
		}
			
		this.type = type;
	}

	/**  
	 * 设置name  
	 * @param name name  
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**  
	 * 设置是否唯一  
	 * @param unique 是否唯一  
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	/**
	 * {@inheritDoc}   
	 */
	public boolean isAutoInc() {
		return this.autoInc;
	}
}