package com.esen.jdbc;

/**
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 * 20090813
 * 字段属性增加是否为空，是否唯一属性；
 * @author yk
 * @version 5.0
 */

public interface AbstractMetaData {
  
  /**
   * 获取字段的数目
   */
  public int getColumnCount();
  
  /**
   * 获取第i个字段的名字, 从0开始
   */
  public String getColumnName(int i);
  
  /**
   * 获取字段的类型，返回值是SqlType中定义的值。 , 从0开始
   */
  public int getColumnType(int i);
  /**
   * 获取字段别名, 从0开始
   * @param i
   * @return
   */
  public String getColumnLabel(int i);
  /**
   * 返回关于此字段的一个描述, 从0开始
   */
  public String getColumnDescription(int i);
  
  /**
   * 返回此字段的长度, 从0开始
   */
  public int getColumnLength(int i);
  
  /**
   * 返回次字段的小数位数 , 从0开始
   */
  public int getColumnScale(int i);
  
  /**
   * 返回字段是否为空；
   * @param i
   * @return
   *    1 表示可为空；
   *    0 表示不可为空；
   *    其他 表示不知道是否可为空；
   */
  public int isNullable(int i);
  
  /**
   * 返回字段是否唯一；
   * @param i
   * @return
   *    1 表示唯一；
   *    0 表示不唯一；
   *    其他 表示不知道是否唯一；
   */
  public int isUnique(int i);
}
