package com.esen.jdbc.dialect;

public interface TableIndexMetaData {

  /**
   * 索引名
   * @return
   */
  public String getName();

  /**
   * 是否唯一
   * @return
   */
  public boolean isUnique();

  /**
   * 索引包含的字段
   * @return
   */
  public String[] getColumns();
}
