package com.esen.jdbc.dialect;

import java.sql.SQLException;

import com.esen.jdbc.AbstractMetaData;

/**
 * 描述一个数据库表或视图的结构信息的类
 * 
 */
public interface TableMetaData extends AbstractMetaData {

	/**
	 * 返回主键,主键可以为空
	 * @return
	 * @throws Exception
	 */
	public String[] getPrimaryKey();

	/**
	 * 返回字段列；
	 * 与getColumn(int i)的区别是：
	 * 总返回新的字段列数组，需要开辟新的内存空间；
	 * 
	 * @deprecated 如果没有必要，尽量用getColumn(int i)和getColumnCount()代替此方法；
	 * @return
	 */
	public TableColumnMetaData[] getColumns();

	/**
	 * 返回索引
	 * @return
	 * @throws Exception
	 */
	public TableIndexMetaData[] getIndexes();

	/**
	 * 获得该对象描述的表的表名, 从0开始
	 * @return
	 */
	public String getTableName();

	/**
	 * 通过字段名获取字段属性对象；
	 * 使用map实现；
	 * 不区分大小写；
	 * @param colname
	 * @return
	 */
	public TableColumnMetaData getColumn(String colname);

	/**
	 * 遍历字段属性对象；
	 * @param i
	 * @return
	 */
	public TableColumnMetaData getColumn(int i);

	/**
	 * 字段个数；
	 */
	public int getColumnCount();

	/**
	 * feild是否是tablename的字段；
	 * @param field 忽略大小写
	 * @return
	 */
	public boolean haveField(String field);

	/**
	 * 获得field对应的真实数据库表字段名(大小写)；
	 * 有些数据库的表名是大小写敏感的，这里传入一个不考虑大小写的字段名，返回一个数据库可以接受的字段名
	 * 对于表名大小写不敏感的数据库可以直接return field
	 * 
	 * 
	 * 
	 * @param field 
	 * 		如果field为空，则返回null;
	 * 		如果没有这个字段，则返回null;
	 * @return
	 */
	public String getRealFieldName(String field);

	/**
	 * 
	 * 
	 * @param field 
	 * 		字段名，如果field为空，出异常提示；如果没有这个字段，出异常提示；
	 * @return 
	 * 		返回field字段类型：'C','I','N','D'...P
	 */
	public char getFieldType(String field);

	/**
	 * 返回数据库字段类型；
	 * 
	 * @param field
	 * 		如果field为空，出异常提示；
	 * 		如果没有这个字段，出异常提示；
	 * @return
	 */
	public int getFieldSqlType(String field);

	/**
	 * 随机选择几个就行了
	 */
	public static final int FIELD_SAMPLE_SELECTRANDOM = 0x1;
	
	/**
	 * distinct所有的行，并返回
	 */
	public static final int FIELD_SAMPLE_DISTINCTALL = 0x2;
	
	/**
	 * 取最大值
	 */
	public static final int FIELD_SAMPLE_MAX = 0x4;
	
	/**
	 * 对一个字段的可能出现的值进行采样，并已对象数组的形式返回采样结果。
	 * 
	 * @param field 字段名，忽略大小写的
	 * @param how_to_sample，采样方法 
	 * @see FIELD_SAMPLE_SELECTRANDOM
	 * @see FIELD_SAMPLE_DISTINCTALL
	 * @return 以对象数组的形式返回采样的结果，如果没有任何数据，那么返回null
	 */
	public Object[] getFieldSample(String field, int how_to_sample) throws SQLException;
}
