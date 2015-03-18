package com.esen.jdbc.dialect.impl.kingbasees;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;

/**
 * 支持人大金仓数据库
 * @author dw
 *
 */
public class KingBaseESDataBaseInfo extends AbstractDataBaseInfo {

  public KingBaseESDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn, defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_KINGBASE_ES;
    this.testSql = "select 1";
  }
  
  /**
   * SELECT CURRENT_SCHEMA()
   */
  protected String getDefaultSchema(Connection conn) {
    try {
      Statement stat = conn.createStatement();
      try {
        ResultSet rs = stat.executeQuery("SELECT CURRENT_SCHEMA()");
        if (rs.next()) {
          return rs.getString(1);
        }
        rs.close();
      }
      finally {
        stat.close();
      }
    }
    catch (SQLException se) {
      se.printStackTrace();
    }
    return null;
  }

  protected String getNCharByteLengthSQL() {
    return "select length('中')";
  }

  protected String getNCharLengthSQL() {
    return "select length('中')";
  }

  public int getMaxIndexOfFieldsLength() {
    return -1;
  }

  public int getMaxKeyOfFieldsLength() {
    return -1;
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
