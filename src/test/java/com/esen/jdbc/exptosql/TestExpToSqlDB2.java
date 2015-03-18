package com.esen.jdbc.exptosql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.exp.Expression;

public class TestExpToSqlDB2 extends TestExpToSql {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.42:50000/bidb",
        "db2admin", "db2admin","debug");
  }
  public void testDiv() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression("NUM_/INT_");
    exp.compile(ch);
    String sql = tosql.toSqlExp(exp);
    assertEquals("case when INT_=0 then null else double(NUM_)/INT_ end ",sql);
  }
}
