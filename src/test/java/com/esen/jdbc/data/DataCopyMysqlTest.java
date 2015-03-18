package com.esen.jdbc.data;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DataCopyMysqlTest extends DataCopyTest {

  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=utf8",
        "root", "dw","debug");
  }

}
