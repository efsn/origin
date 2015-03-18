package com.esen.jdbc.table;

/**
 * 修改数据库表的结构，提供增删改字段的方法
 * 
 * 数据库的操作分为DML和DDL:
 * DML(Data Manipulation Language):数据操纵语言，用户可以查询数据库和操作已经有数据库中的数据，如insert,delete,update,select
 * DDL(Data Definition Language)数据库定义语言，如create,alter和drop。DDL是隐性提交的，不能rollback。
 * 
 * 此类实现了DDL的rollback.如果对表的结构有多个修改，要么都成功，要么都不成功
 *
 * 此类的调用方法：
 * 
 * AlterTable at = xx;
 * at.addField(xx);
 * at.removeField(xx)
 * try{
 *   at.commit();
 * }catch(Exception e){
 *   //TODO 抛出异常或进行其它处理
 * }
 * @author zhuchx
 */
public interface AlterTable {

	/**
	 * 增加自动增长字段
	 * @param fieldName 字段名
	 * @param step 每次自动增加的数值
	 * @deprecated 暂时不支持此方法
	 */
	//public void addAutoIncField(String fieldName, int step);

	/**
	 * 增加字段
	 * @param field 字段名
	 * @param type 字段类型，参考DbDefiner.FIELD_TYPE_XX
	 * @param len 字段长度
	 * @param sacle 字段小数位数
	 * @param defaultvalue 字段默认值
	 * @param nullable 字段值是否可为空
	 * @param unique 字段值是否唯一
	 */
	public void addField(String fieldName, char type, int len, int sacle, String defaultvalue, boolean nullable,
			boolean unique);

	/**
	 * 删除字段
	 * @param fieldName 需要删除的字段
	 */
	public void dropField(String fieldName);

	/**
	 * 修改字段
	 * @param oldFieldName 旧的字段名
	 * @param newFieldName 新的字段名,为空时表示不修改字段名
	 * @param type 新字段的类型，参考DbDefiner.FIELD_TYPE_XX
	 * @param len 新字段的长度
	 * @param sacle 新字段的小数位数
	 */
	public void modifyField(String oldFieldName, String newFieldName, char type, int len, int sacle);

	/**
	 * 修改字段
	 * @param oldFieldName 旧的字段名
	 * @param newFieldName 新的字段名,为空时表示不修改字段名
	 * @param type 新字段的类型，参考DbDefiner.FIELD_TYPE_XX
	 * @param len 新字段的长度
	 * @param sacle 新字段的小数位数
	 * @param defaultvalue 新字段默认值
	 * @param nullable 新字段值是否可为空
	 * @param unique 新字段值是否唯一
	 */
	public void modifyField(String oldFieldName, String newFieldName, char type, int len, int sacle,
			String defaultvalue, boolean nullable, boolean unique);

	/**
	 * 修改字段的描述信息
	 * @param fieldName 字段名
	 * @param desc 新的字段描述信息
	 */
	public void modifyFieldDesc(String fieldName, String desc);

	/**
	 * 提交对表的修改，如果出现异常，会自动回滚，不需要外部调用回滚
	 * @throws Exception
	 */
	public void commit() throws Exception;
}
