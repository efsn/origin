package com.esen.jdbc.dialect.impl.oracle;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;

public class Oracle817Def extends OracleDef {

  public Oracle817Def(Dialect dl) {
    super(dl);
  }
  /**
   * Oracle没有time,timestamp数据类型，它用date表示；
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATE " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIME, isUpdate);
  }
  /**
   * Oracle没有time,timestamp数据类型，它用date表示；
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATE " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIMESTAMP, isUpdate);
  }
}
