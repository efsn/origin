package com.esen.jdbc.data;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DataCopyDB2Test extends DataCopyTest {

  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.42:50000/bidb",
        "db2admin", "db2admin","debug");
  }

}
