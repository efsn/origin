package com.esen.jdbc.orm.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author liujin
 * 
 */
public class ImportDataHelper {
	/**
	 * 直接添加数据
	 */
	public static final int OPT_ADD = 0;
	
	/**
	 * 先清空表再插入数据
	 */
	public static final int OPT_CLEARTABLE = 1;

	/**
	 * 追加记录 
	 * 目的表存在主键，将只插入新记录，已存在的记录被忽略
	 */
	public static final int OPT_APPEND = 2;

	/**
	 * 更新记录 
	 * 如果目的表没有主键或者唯一约束，直接写入； 
	 * 如果目的表有主键或者唯一约束，写入新记录，更新存在的记录；
	 */
	public static final int OPT_UPDATE = 3;

	/**
	 * 目的表的字段名 和 数据文件中的字段的序号（从 0 开始）的对应关系
	 * 数据文件中不存在的字段名序号为 -1
	 */
	private Map<String, Integer> nameMapping;

	private Map<String, Object> defaultValue;

	private int option = OPT_ADD;

	private char separator = '\t';

	/**
	 * 设置需要导入实体对象的表的字段名 和 数据文件中的字段的序号的对应关系
	 * 
	 * @param nameMapping
	 */
	public void setNameMapping(Map<String, Integer> nameMapping) {
		this.nameMapping = nameMapping;
	}
	
	/**
	 * 添加需要导入实体对象的表的字段名 和 数据文件中的字段的序号的对应关系
	 * 
	 * @param fieldName 字段名
	 * @param index 字段序号，从 0 开始， -1 标识不存在
	 */
	public void addNameMapping(String fieldName, Integer index) {
		if (this.nameMapping == null) {
			this.nameMapping = new HashMap();
		}
		this.nameMapping.put(fieldName, index);
	}

	/**
	 * 获取需要导入实体对象的表的字段名 和 数据文件中的字段的序号的对应关系
	 * 
	 * @return
	 */
	public Map<String, Integer> getNameMapping() {
		return this.nameMapping;
	}

	/**
	 * 设置导入数据的选项
	 * 
	 * @param option
	 */
	public void setOption(int option) {
		if (option == OPT_CLEARTABLE || option == OPT_APPEND || option == OPT_UPDATE) {
			this.option = option;
		} else {
			this.option = OPT_ADD;
		}
	}

	/**
	 * 获取导入数据的选项
	 * 
	 * @return
	 */
	public int getOption() {
		return this.option;
	}

	/**
	 * 添加需要导入数据的实体对象的属性的默认值
	 * 
	 * @param propertyName
	 *            实体属性名
	 * @param defaultValue
	 *            默认值
	 */
	public void addDefaultValue(String propertyName, Object defaultValue) {
		if (this.defaultValue == null) {
			this.defaultValue = new HashMap();
		}
		this.defaultValue.put(propertyName, defaultValue);
	}
	
	/**
	 * 设置需要导入数据的实体对象的属性的默认值
	 * 
	 * @param defaultValue
	 *            默认值
	 */
	public void setDefaultValue(Map defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * 获取属性的默认值
	 * 
	 * @return
	 */
	public Map getDefaultValueMap() {
		return this.defaultValue;
	}

	/**
	 * 设置数据的分隔符，默认为 tab
	 * 
	 * @param separator
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	/**
	 * 获取分隔符
	 * 
	 * @return 分隔符
	 */
	public char getSeparator() {
		return this.separator;
	}
}
