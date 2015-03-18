package com.esen.jdbc;

import java.sql.*;


import junit.framework.TestCase;

public class TestDataBase extends TestCase {

	public void testQurey() throws Exception{
		Class.forName("dm.jdbc.driver.DmDriver");
	    Connection conn = DriverManager.getConnection(
	        "jdbc:dm://192.168.1.224:12345/testdb", "test", "111111");
	    try{
	    	Statement stat = conn.createStatement();
	    	ResultSet rs = stat.executeQuery("select length('中文a')");
	    	rs.next();
	    	assertEquals(3, rs.getInt(1));
	    	rs.close();
	    	stat.close();
	    }finally{
	    	conn.close();
	    }
	}
}
