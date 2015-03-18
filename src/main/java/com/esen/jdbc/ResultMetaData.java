package com.esen.jdbc;

import java.io.Serializable;

/**
 * @author yk
 */

public interface ResultMetaData extends AbstractMetaData, Serializable{
  /**
   * 将数据库的字段类型getColumnType(i)转换成常用的char：'C','N','I','D',...
   * 原来的方法在AbstractMetaData已经存在；
   */
  public char getColumnTypeStr(int i);

}
