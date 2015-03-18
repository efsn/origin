package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectSqlServer2005 extends TestDialect {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        "jdbc:sqlserver://192.168.1.222:1433;databaseName=testdb;",
        "testcase", "testcase","debug");
}
}
