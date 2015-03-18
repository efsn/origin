package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

public class SybaseDataBaseInfo extends AbstractDataBaseInfo {

  public SybaseDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn, defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_SYBASE;
    this.testSql = "select 1";
  }

  protected String getNCharByteLengthSQL() {
    return "select datalength('中')";
  }
  
  protected String getNCharLengthSQL() {
    return "select datalength('中')";
  }

  public int getMaxKeyOfFieldsLength() {
    return 2600;
  }

  public int getMaxIndexOfFieldsLength() {
    return 2600;
  }

  /**
   * 创建表时可能会调用这个方法，获取字符字段允许的定义长度；
   * 这里给一个一般情况下，比较大的字符字段定义长度，而不是数据库允许的最大长度；
   * 原因是如果这个值设置的很大，尽管数据允许，但是和表的其他字段长度加起来可能超过页大小限制；
   */
  public int getMaxVarcharFieldLength() {
    return 4000;
  }
  
}
