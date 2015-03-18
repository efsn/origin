package com.esen.jdbc.data;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDataCopyForCSVSybaseAse extends TestDataCopyForCSV {

/*  protected void initConnectionFactory() {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    poolname = "dw_sybasease";
    SimpleConnectionFactory db = new SimpleConnectionFactory(
        "com.sybase.jdbc3.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.102:5000/bidb?charset=cp936",
        "test", "111111","debug");
    fm.setConnectionFactory(poolname,db);
    DefaultConnectionFactory.set(fm);

  }*/
  protected void initConnectionFactory() {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    poolname = "dw_sybasease";
    SimpleConnectionFactory db = new SimpleConnectionFactory(
        "com.sybase.jdbc3.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.102:5000/bidb?charset=utf8",
        "test", "111111","debug");
    fm.setConnectionFactory(poolname,db);
    DefaultConnectionFactory.set(fm);

  }
}
