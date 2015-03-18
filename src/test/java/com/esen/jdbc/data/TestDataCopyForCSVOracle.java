package com.esen.jdbc.data;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDataCopyForCSVOracle extends TestDataCopyForCSV {
  protected void initConnectionFactory(){
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    poolname = "200_oracle_tax";
    SimpleConnectionFactory oracle = new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:esenbi", "test", "test","debug");
    fm.setConnectionFactory(poolname,oracle);
    DefaultConnectionFactory.set(fm);
    
  }
}
