package com.esen.jdbc.dialect.impl.db2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class DB2DbMetaData extends DbMetaDataImpl {

  public DB2DbMetaData(Connection con) {
    super(con);
  }
  public DB2DbMetaData(ConnectionFactory dbf) {
    super(dbf);
  }
  
  /**
   * 20091020
   * 这里需要将DB2的物化视图列出来；
   * 改用查系统表：
   * select tabname,type from SYSCAT.TABLES where owner='DB2ADMIN' and (type='T' or type='S')
   * 
   * 20100301 BI-3019
   * jdbc列出表和视图，现在需要扩大范围，有些比如Oracle的同义词（已经支持），
   * db2的类型化表（typed table）,还有物化视图等现在不能显示出来；
   * 帖子中提到的没显示出来的表，类型是 U 
   * 
   * 现在此方法改成广义上的返回除视图外的所有对象；
   * 
   * Type of object:
    * A = Alias
    * G = Global temporary table
    * H = Hierarchy table
    * L = Detached table
    * N = Nickname
    * S = Materialized query table
    * T = Table (untyped)
    * U = Typed table
    * V = View (untyped)
    * W = Typed view
   */
  protected ArrayList selectAllTableNames() {
    try {
      Connection conn = getConnection();
      /**
       * BI-3717
       * Db2 8 SYSCAT.TABLES 中没有owner字段，为了兼容使用DEFINER字段；
       * 参考：http://publib.boulder.ibm.com/infocenter/db2luw/v9r5/index.jsp?topic=/com.ibm.db2.luw.sql.ref.doc/doc/r0001063.html
       * 20110714 数据库用户可以在多个schema中创建表，这里是列出默认schema下的所有表，采用TABSCHEMA字段进行判断才是正确的。
       */
      String sql = "select tabname from SYSCAT.TABLES where TABSCHEMA='"+this.getSchemaName()+"' " +
                           " and type<>'V'";
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
      //同selectAllTableNames 方法；
      String sql = "select tabname from SYSCAT.TABLES where TABSCHEMA='"+this.getSchemaName()+"' " +
                           " and type='V'";
      return querySqlForTables(conn, sql);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  protected TableMetaData createTableMetaData(String tablename){
    return new DB2TableMetaData(this,tablename);
  }
}
