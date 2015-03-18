package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectMySql extends TestDialect {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://192.168.1.223/testdb?useUnicode=true&characterEncoding=utf8",
        "testcase", "testcase","debug");
  }
}
