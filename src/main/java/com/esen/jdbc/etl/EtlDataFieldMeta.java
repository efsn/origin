package com.esen.jdbc.etl;

public interface EtlDataFieldMeta {

  /**
   * 获得字段名
   */
  public String getFieldName();

  /**
   * 获取类型
   */
  public char getDataType();

  /**
   * 获取描述，可能为空
   */
  public String getDescription();

  /**
   * 是否维表字段
   * @return
   */
  public boolean isDimField();

  /**
   * 如果是维表字段，则返回对应的维表名称
   * @return
   */
  public String getDimName();

  /**
   * 获取唯一序号，ireport特有的东西，其它源返回－1
   */
  public int getUniquedIndex();

  /**
   * 返回字段长度
   * @return
   */
  public int getLength();

  /**
   * 返回小数位数
   * @return
   */
  public int getScale();

  /**
   * 获得字段在表中的序号
   */
  public int getFiledIndex();

  /**
   * 返回对应的数据库字段
   * @return
   */
  public String getDbField();
}
