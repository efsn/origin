package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectGBase8t extends TestDialect {
	public ConnectionFactory createConnectionFactory() {
		return new SimpleConnectionFactory(
				"com.informix.jdbc.IfxDriver",
				"jdbc:informix-sqli://172.21.50.111:1543/jdbc_test_utf8:INFORMIXSERVER=ol_informix1210_5;DB_LOCALE=en_us.utf8",
				"root", "sanlink", "debug");

	}

	protected String getSqlValue(String str) {
		return "SELECT " + str + " from SYSMASTER:SYSSHMVALS";
	}

	public void testCountSql() throws Exception {
		createTable();
		addData(10);
		Dialect dl = getConnectionFactory().getDialect();
		String querySelect = "select sum(NUM_) as num_,"
				+ dl.funcLeft("STR_", "1") + " as b from " + tablename
				+ " group by b" + " order by b";
		String sql = dl.getCountString(querySelect);
		String sql2 = "SELECT COUNT(*) FROM(select sum(NUM_) as num_,"
				+ dl.funcLeft("STR_", "1") + " as b from " + tablename
				+ " group by b" + ") x_";
		assertEquals(sql, sql2);
		assertEquals(true, excuteQuery(sql));
	}
}
