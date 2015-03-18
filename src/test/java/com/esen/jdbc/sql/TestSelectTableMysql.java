package com.esen.jdbc.sql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestSelectTableMysql extends TestSelectTable {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://192.168.1.42/test?useUnicode=true&characterEncoding=GB2312",
        "root", "dw",true);
  }
}
