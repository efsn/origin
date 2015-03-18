package com.esen.jdbc.etl;

import java.sql.Date;

public interface EtlDataDest {
  /**
   * 设置一个字段的值，
   * value可能是String，Integer，Double，Date等
   */
  public void setValue(int fieldIndex, Object value);
}
