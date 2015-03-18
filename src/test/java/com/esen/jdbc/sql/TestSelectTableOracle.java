package com.esen.jdbc.sql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestSelectTableOracle extends TestSelectTable {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:esenbi",
        "test", "test","debug");
  }
}
