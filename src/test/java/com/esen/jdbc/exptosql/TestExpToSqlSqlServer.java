package com.esen.jdbc.exptosql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestExpToSqlSqlServer extends TestExpToSql {
  public ConnectionFactory createConnectionFactory(){
    //sql server 2000
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.42:1433/dwdb",
        "dw", "dw","debug");
  }
}
