package com.esen.jdbc.sql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestSelectTableDB2 extends TestSelectTable {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.42:50000/dwdb",
        "db2admin", "db2admin",true);
  }

}
