package com.esen.jdbc.exptosql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestExpToSqlDM extends TestExpToSql {
	public ConnectionFactory createConnectionFactory() {
		return new SimpleConnectionFactory(
		        "dm.jdbc.driver.DmDriver",
		        "jdbc:dm://192.168.1.224:12345/testdb?enconding=GBK",
		        "test", "111111","debug");
	}
}
