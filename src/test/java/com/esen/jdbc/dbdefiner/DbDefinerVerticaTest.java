package com.esen.jdbc.dbdefiner;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DbDefinerVerticaTest extends DbDefinerTest {

 public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
	        "com.vertica.jdbc.Driver",
	        "jdbc:vertica://172.21.10.220/VMart",
	        "dbadmin", "password", "debug");
  }
}
