package com.esen.jdbc.dialect.impl;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;

public class OtherDataBaseInfo extends AbstractDataBaseInfo {

  public OtherDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn,defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_OTHER;
    this.testSql = "select 1";
  }
  protected String getNCharByteLengthSQL() {
    return null;
  }
  
  protected String getNCharLengthSQL() {
    return null;
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
