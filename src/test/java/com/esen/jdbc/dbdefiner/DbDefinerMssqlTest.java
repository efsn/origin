package com.esen.jdbc.dbdefiner;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
/**
 * mssql2000
 * @author dw
 *
 */
public class DbDefinerMssqlTest extends DbDefinerTest {

/*  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.21:1433;databaseName=test;",
        "esen", "esen","debug");
  }*/
/*  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.51:1434;databaseName=test;",
        "esen2", "esen2","debug");
  }*/

  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.223:1433;databaseName=testdb;",
        "testcase", "testcase","debug");
  }
  /*public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.223:1433/testdb",
        "testcase", "testcase","debug");
  }*/
/*  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.21:1433/test",
        "esen", "esen","debug");
  }*/
  
/*  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.165:1078/rth",
        "zjw", "zjw","debug");
  }*/
}
