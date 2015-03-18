package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectDM extends TestDialect {
	public ConnectionFactory createConnectionFactory() {
		return new SimpleConnectionFactory(
		        "dm.jdbc.driver.DmDriver",
		        "jdbc:dm://192.168.1.224:12345/testdb",
		        "test", "111111","debug");
	}
}
