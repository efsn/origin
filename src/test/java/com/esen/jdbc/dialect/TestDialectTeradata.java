package com.esen.jdbc.dialect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectTeradata extends TestDialect {

  public ConnectionFactory createConnectionFactory(){
	    return new SimpleConnectionFactory(
	        "com.teradata.jdbc.TeraDriver",
	        "jdbc:teradata://192.168.226.128/CLIENT_CHARSET=GBK",
	        "testuser", "testpassword", "error");
	  }
}
