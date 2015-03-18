package com.esen.jdbc.dbdefiner;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class DbDefinerGBase8tTest extends DbDefinerTest {

	public ConnectionFactory createConnectionFactory() {
		return new SimpleConnectionFactory(
			"com.informix.jdbc.IfxDriver",
			// "jdbc:informix-sqli://172.21.50.111:1543/test_jdbc:informixserver=ol_informix1210_5;",
			// "root", "sanlink", "debug");

			"jdbc:informix-sqli://172.21.50.111:1543/jdbc_test_utf8:INFORMIXSERVER=ol_informix1210_5;DB_LOCALE=en_us.utf8",
			"root", "sanlink", "debug");
	}

}