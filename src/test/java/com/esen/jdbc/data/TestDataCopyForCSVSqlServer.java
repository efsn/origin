package com.esen.jdbc.data;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDataCopyForCSVSqlServer extends TestDataCopyForCSV {

  protected void initConnectionFactory() {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    poolname = "dw_sqlserver";
    SimpleConnectionFactory db =  new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.102:1433/dwdb",
        "dw", "dw","debug");
    fm.setConnectionFactory(poolname,db);
    DefaultConnectionFactory.set(fm);

  }

}
