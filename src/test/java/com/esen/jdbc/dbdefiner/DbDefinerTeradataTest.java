package com.esen.jdbc.dbdefiner;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DbDefinerTeradataTest extends DbDefinerTest {

 public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
	        "com.teradata.jdbc.TeraDriver",
	        "jdbc:teradata://192.168.226.128/CLIENT_CHARSET=GBK",
	        "testuser", "testpassword", "debug");
  }
}
