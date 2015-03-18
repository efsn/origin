package com.esen.jdbc.dialect.impl.oscar;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class OscarDataBaseInfo extends AbstractDataBaseInfo {

  public OscarDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn,defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_OSCAR;
    this.testSql = "select 1 from dual";
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
