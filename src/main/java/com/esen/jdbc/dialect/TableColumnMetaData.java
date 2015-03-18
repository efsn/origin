package com.esen.jdbc.dialect;

public interface TableColumnMetaData {
  /**
   * 字段名
   * @return
   */
  public String getName();

  /**
   * 标题
   * @return
   */
  public String getLabel();

  /**
   * 描述信息
   * @return
   */
  public String getDesc();

  /**
   * 字段长度
   * @return
   */
  public int getLen();

  /**
   * 小数位数
   * @return
   */
  public int getScale();

  /**
   * 字段类型
   * @return
   */
  public int getType();

  /**
   * 是否自动增长字段
   * @return
   */
  public boolean isAutoInc();

  /**
   * 是否可为空
   * @return
   */
  public boolean isNullable();

  /**
   * 是否唯一
   * @return
   */
  public boolean isUnique();
  /**
   * 获取默认值
   * @return
   */
  public String getDefaultValue();
}
