package com.esen.jdbc.dialect.impl.db2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

/**
 * 构造AS400数据库的DbMetaData实现；
 * @author dw
 *
 */
public class DB2AS400DbMetaData extends DbMetaDataImpl {

	public DB2AS400DbMetaData(Connection con) {
		super(con);
	}

	public DB2AS400DbMetaData(ConnectionFactory dbf) {
		super(dbf);
	}
	
	 /**
   * DB2 AS400上没有SYSCAT表只有SYSIBM系统表, 这里用如下查询SQL：
   * SELECT TABLE_NAME FROM SYSIBM.TABLES WHERE TABLE_SCHEMA=? AND TABLE_TYPE=?
   * 第二个参数TABLE_TYPE有以下取值： ALIAS \ BASE TABLE \ VIEW
   * 注意这里的字段名和DB2下的SYSCAT表也有一定的差异
   */
  protected ArrayList selectAllTableNames() {
  	try {
    	Connection conn = getConnection();
      String sql = "select TABLE_NAME from SYSIBM.TABLES where TABLE_SCHEMA='"+this.getSchemaName()+"' " +
                           " and TABLE_TYPE='BASE TABLE'";
      return querySqlForTables(conn, sql);
  	}
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  private ArrayList querySqlForTables(Connection conn, String sql) throws SQLException {
    try {
      Statement stat = conn.createStatement();
      ResultSet _rs = stat.executeQuery(sql);
      ArrayList l = new ArrayList(32);
      try {
        while (_rs.next()) {
          String tbName = _rs.getString(1);
          l.add(tbName);
        }
      }
      finally {
        if(stat!=null)
          stat.close();
        if (_rs != null)
          _rs.close();
      }
      return l;
    }
    finally {
      closeConnection(conn);
    }
  }

  protected ArrayList selectAllViewNames() {
    try {
    	Connection conn = getConnection();
    	String sql = "select TABLE_NAME from SYSIBM.TABLES where TABLE_SCHEMA='"+getSchemaName()+"' " +
                           " and TABLE_TYPE='VIEW'";
    	return querySqlForTables(conn, sql);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

	protected TableMetaData createTableMetaData(String tablename) {
		return new DB2AS400TableMetaData(this, tablename);
	}
}
