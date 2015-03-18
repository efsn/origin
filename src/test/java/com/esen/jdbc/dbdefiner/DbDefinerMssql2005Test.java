package com.esen.jdbc.dbdefiner;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableMetaData;

public class DbDefinerMssql2005Test extends DbDefinerTest {

  /**
   * jdbc:jtds:sqlserver://192.168.1.2:1433/shlbak;TDS=8.0;SendStringParametersAsUnicode=true  as/as
   * jdbc:jtds:sqlserver://192.168.1.2:1433/dwtest;TDS=8.0;SendStringParametersAsUnicode=true;charset=GBK  dw2/dw2
   * 
   */

/*  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.222:1433/testdb",
        "testcase", "testcase","debug");
  }*/
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.102:1433;databaseName=testdb;",
        "test", "test","debug");
  }
 /* public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.222:1433;databaseName=zjwdemo;",
        "zjw", "zjw","debug");
  }*/
/*  public void testGetTableName() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      assertEquals(true,dbv.tableExists(conn,null,"dim_zjj"));
      assertEquals(false,dbv.viewExists(conn,"dim_zjj"));
    }finally{
      conn.close();
    }
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData("dim_zjj");
    tmd.getColumnCount();
  }*/
  
  
}
