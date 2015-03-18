package com.esen.jdbc.data;

import com.esen.jdbc.ConnectFactoryManagerImpl;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDataCopyForCSVDB2 extends TestDataCopyForCSV {

  protected void initConnectionFactory() {
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    poolname = "dw_db28";
    SimpleConnectionFactory db2 = new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.102:50000/bidb", "db2admin", "db2admin","debug");
    fm.setConnectionFactory(poolname,db2);
    DefaultConnectionFactory.set(fm);

  }

}
