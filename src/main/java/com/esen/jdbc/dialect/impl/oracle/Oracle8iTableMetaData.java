package com.esen.jdbc.dialect.impl.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

/**
 * Oracle8的表结构实现类；
 * @author dw
 *
 */
public class Oracle8iTableMetaData extends OracleTableMetaData {

  public Oracle8iTableMetaData(DbMetaDataImpl owner, String tablename) {
    super(owner, tablename);
  }
  /**
   * Oracle8 也可以考虑调用jdbc系统函数获取key，有环境测试在改；
   */
  protected synchronized void initPrimaryKey()  {
    String sql = "select constraint_name from user_constraints where constraint_type='P' and table_name=\'"
        + this.getTableName() + "\'";
    String indexname = null;
    try{
    Connection con = this.owner.getConnection();
    try {
      Statement sm = con.createStatement();
      try {
        ResultSet rs = sm.executeQuery(sql.toUpperCase());
        try {
          if (rs.next()) {
            indexname = rs.getString(1);
          }
        }
        finally {
          rs.close();
        }
      }
      finally {
        sm.close();
      }
    }
    finally {
      this.owner.closeConnection(con);
    }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (indexname != null) {
      TableIndexMetaData[] idx = getIndexes();
      if (idx != null && idx.length != 0) {
        for (int i = 0; i < idx.length; i++) {
          if (idx[i].getName().equalsIgnoreCase(indexname)) {
            this.setPrimaryKey(idx[i].getColumns());
            break;
          }
        }
      }
    }
  }
}
