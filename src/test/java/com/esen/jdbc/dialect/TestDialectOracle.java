package com.esen.jdbc.dialect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectOracle extends TestDialect {
/*  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.223:1521:testdb",
        "testcase", "testcase","debug");
  }*/
  public ConnectionFactory createConnectionFactory(){
	    return new SimpleConnectionFactory(
	        "oracle.jdbc.driver.OracleDriver",
	        "jdbc:oracle:thin:@192.168.1.102:1521:orcl",
	        "test", "test","debug");
	  }
  protected String getSqlValue(String str) {
    return "select "+str+" from dual";
  }
  public void testStrConcat() throws Exception{
    createTable();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = getConnectionFactory().getConnection();
    try{
      Statement stat = conn.createStatement();
      stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values(null,1)");
      stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values('',2)");
      ResultSet rs = stat.executeQuery("select STR_,"
          +dl.funcStrCat("STR_", "'aa'")
          +","+dl.funcStrCat("'aa'", "STR_")
          +","+dl.funcStrCat(null, "STR_")
          +","+dl.funcStrCat("''", "STR_")
          +","+dl.funcStrCat(null, "'bb'")
          +" from "+tablename+" order by NUM_");
      rs.next();
      assertEquals(null,rs.getString(1));
      assertEquals(true,rs.wasNull());
      assertEquals("aa",rs.getString(2));
      assertEquals("aa",rs.getString(3));
      assertEquals(null,rs.getString(4));
      assertEquals(null,rs.getString(5));
      assertEquals("bb",rs.getString(6));
      rs.next();
      assertEquals(null,rs.getString(1));
      assertEquals(true,rs.wasNull());
      assertEquals("aa",rs.getString(2));
      assertEquals("aa",rs.getString(3));
      assertEquals(null,rs.getString(4));
      assertEquals(null,rs.getString(5));
      assertEquals("bb",rs.getString(6));
      
      rs = stat.executeQuery("select count(*) from "+tablename+" where STR_ is null");
      rs.next();
      assertEquals(2,rs.getInt(1));
      
      rs = stat.executeQuery("select count(*) from "+tablename+" where STR_=''");
      rs.next();
      assertEquals(0,rs.getInt(1));
      
      stat.close();
    }finally{
      conn.close();
    }
  }
  protected void tryBlobClobNull() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.funcToSqlConst(null, Types.BLOB);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.VARBINARY);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.BINARY); 
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToSqlConst(null, Types.LONGVARCHAR); 
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.LONGVARBINARY);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.CLOB);
    assertEquals(true, excuteQuery(getSql(str)));
  }
}
