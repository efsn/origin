package com.esen.jdbc.data;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DataCopyOracleTest extends DataCopyTest {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.100:1521:esenbi",
        "test", "test","debug");
  }
  
  
}
