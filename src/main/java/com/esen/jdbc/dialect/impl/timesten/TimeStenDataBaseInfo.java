package com.esen.jdbc.dialect.impl.timesten;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class TimeStenDataBaseInfo extends AbstractDataBaseInfo {

  public TimeStenDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn, defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_TIMESTEN;
    this.testSql = "select 1 from dual";
  }
  
  protected String getNCharByteLengthSQL() {
    return "select lengthb('中') from dual";
  }
  
  protected String getNCharLengthSQL() {
    return "select lengthb('中') from dual";
  }

  public int getMaxKeyOfFieldsLength() {
    return -1;
  }
  public int getMaxIndexOfFieldsLength() {
    return -1;
  }

  public int getMaxVarcharFieldLength() {
    return -1;
  }
}
