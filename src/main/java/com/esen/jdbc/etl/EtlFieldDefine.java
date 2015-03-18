package com.esen.jdbc.etl;

/**
 * 定义一个bi主题中的一个field的抽取关系
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 * @author yk
 * @version 5.0
 */
public interface EtlFieldDefine {
  /**
   * 目的字段的名称
   */
  public String getFieldName();

  /**
   * 返回字段的描述信息
   * @return
   */
  public String getCaption();

  /**
   * 源表达式
   */
  public String getSrcExp();

  /**
   * 返回字段类型
   * @return
   */
  public char getDataType();

  /**
   * 返回字段长度
   * @return
   */
  public int getLength();

  /**
   * 返回字段的小数位数
   * @return
   */
  public int getScale();
}
