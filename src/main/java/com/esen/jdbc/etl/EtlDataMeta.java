package com.esen.jdbc.etl;

import java.sql.Date;

public interface EtlDataMeta {

  /**
  * 返回表名
  * @return
  */
  public String getName();

  /**
   * 返回描述，可能为null
   */
  public String getCaption();

  /**
   * 获得字段数目
   */
  public int getFieldCount();

  /**
   * 获得第i个字段的名称
   */
  public EtlDataFieldMeta getField(int i);

  /**
   * 获得field根据名称，不存在返回null
   */
  public EtlDataFieldMeta getField(String fldname);
}
