package com.esen.jdbc.data;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDataCopyForCSVMysql extends TestDataCopyForCSV {

/*  protected void initConnectionFactory() {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    poolname = "dw_mysql";
    SimpleConnectionFactory mysql = new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=utf8", "root", "dw","debug");
    fm.setConnectionFactory(poolname,mysql);
    DefaultConnectionFactory.set(fm);

  }*/
  protected void initConnectionFactory() {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    poolname = "dw_mysql";
    SimpleConnectionFactory mysql = new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost/gbkdb?useUnicode=true&characterEncoding=gbk", "root", "dw","debug");
    fm.setConnectionFactory(poolname,mysql);
    DefaultConnectionFactory.set(fm);

  }
}
