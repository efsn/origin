package com.esen.jdbc.sql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestSelectTableSybase extends TestSelectTable {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc2.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.42:5000/bidb?charset=cp936",
        "sa", "",true);
  }
}
