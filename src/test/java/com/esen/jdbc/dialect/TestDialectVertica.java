package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectVertica extends TestDialect {

  public ConnectionFactory createConnectionFactory(){
	    return new SimpleConnectionFactory(
	        "com.vertica.jdbc.Driver",
	        "jdbc:vertica://172.21.10.220/VMart",
	        "dbadmin", "password", "debug");
	  }
}
