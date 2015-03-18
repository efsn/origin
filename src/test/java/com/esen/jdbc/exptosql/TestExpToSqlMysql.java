package com.esen.jdbc.exptosql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.exp.Expression;

public class TestExpToSqlMysql extends TestExpToSql {
	public ConnectionFactory createConnectionFactory() {
		return new SimpleConnectionFactory("com.mysql.jdbc.Driver",
				"jdbc:mysql://192.168.1.102/testdb?useUnicode=true&characterEncoding=utf8", 
				"testcase", "testcase",
				"debug");
	}
  
  public void testContactStrOp() throws Exception{
	    Dialect dl = getConnectionFactory().getDialect();
	    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
	    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
	    
	    Expression exp = new Expression("field^'A'");
	    exp.compile(ch);
	    assertEquals(" CONCAT(ifnull(FIELD,''),'A')",tosql.toSqlExp(exp));
	   
	    exp = new Expression("field^null");
	    exp.compile(ch);
	    assertEquals("FIELD",tosql.toSqlExp(exp));
	  }
}
