package com.esen.jdbc.dialect.impl.mssql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.util.StrFunc;

public class MssqlDbMetaData extends DbMetaDataImpl {

  public MssqlDbMetaData(Connection con) {
    super(con);
  }
  public MssqlDbMetaData(ConnectionFactory dbf) {
    super(dbf);
  }
  
  
  protected TableMetaData createTableMetaData(String tablename){
    return new MssqlTableMetaData(this,tablename);
  }

  /**
   * 20100327
   * 支持带schema名的表名的访问，并且列出表名总是带上schema名；
   * INFORMATION_SCHEMA.TABLES   是一个视图，包含当前用户有权限的当前数据库中的表。   
   */
  protected ArrayList selectAllTableNames() {
    try {
      Connection conn = getConnection();
      try {
        Statement stat = conn.createStatement();
        String sql = "select TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME,TABLE_TYPE from INFORMATION_SCHEMA.TABLES";
        ResultSet _rs = stat.executeQuery(sql);
        try {
          ArrayList l = new ArrayList(32);
          StringBuffer tb = new StringBuffer(64);
          while (_rs.next()) {
            tb.setLength(0);
            //查询结果中可能有视图，需要去掉
            if ("VIEW".compareToIgnoreCase(_rs.getString(4)) != 0) {
	            String schemaname = _rs.getString(2);
	            String tbName = _rs.getString(3);
	            /*
	             * ESENBI-3128: modify by liujin 2014.12.09
	             * MSSQL中，对象的 schema 名为默认的 schema 时，在显示对象名时不带 schema 名
	             */
	            if (StrFunc.isNull(schemaname)
	            		|| schemaname.compareToIgnoreCase(getSchemaName()) == 0) {
	            	tb.append(tbName);
	            } else {
	            	tb.append(schemaname).append(".").append(tbName);
	            }
	            l.add(tb.toString());
            }
          }
          return l;
        }
        finally {
          if (_rs != null)
            _rs.close();
          if(stat!=null)
        	stat.close();
        }
      }
      finally {
        closeConnection(conn);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * INFORMATION_SCHEMA.VIEWS
   * 同INFORMATION_SCHEMA.TABLES
   */
  protected ArrayList selectAllViewNames() {
    try {
      Connection conn = getConnection();
      try {
        Statement stat = conn.createStatement();
        String sql = "select TABLE_CATALOG,TABLE_SCHEMA,TABLE_NAME from INFORMATION_SCHEMA.VIEWS";
        ResultSet _rs = stat.executeQuery(sql);
        try {
          ArrayList l = new ArrayList(32);
          StringBuffer tb = new StringBuffer(64);
          while (_rs.next()) {
            tb.setLength(0);
            String schemaname = _rs.getString(2);
            String tbName = _rs.getString(3);
            
            /*
             * ESENBI-3128: modify by liujin 2014.12.09
             * MSSQL中，对象的 schema 名为默认的 schema 时，在显示对象名时不带 schema 名
             */
            if (StrFunc.isNull(schemaname)
            		|| schemaname.compareToIgnoreCase(getSchemaName()) == 0) {
            	tb.append(tbName);
            } else {
            	tb.append(schemaname).append(".").append(tbName);
            }
 
            l.add(tb.toString());
          }
          return l;
        }
        finally {
          if (_rs != null)
            _rs.close();
          
          //需要关闭 statement
          if (stat != null)
        	stat.close();
        }
      }
      finally {
        closeConnection(conn);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
}
