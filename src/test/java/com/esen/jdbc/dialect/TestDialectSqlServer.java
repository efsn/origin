package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectSqlServer extends TestDialect {
  public ConnectionFactory createConnectionFactory(){
    //sql server 2000
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.223:1433;databaseName=testdb;",
        "testcase", "testcase","debug");
  }
}
