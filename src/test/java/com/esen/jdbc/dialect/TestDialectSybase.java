package com.esen.jdbc.dialect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectSybase extends TestDialect {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc3.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.222:5000/testdb?charset=cp936",
        "testcase", "testcase","debug");
  }
  public void testStrConcat() throws Exception{
    createTable();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = getConnectionFactory().getConnection();
    try{
      Statement stat = conn.createStatement();
      stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values(null,1)");
      stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values('',2)");//sybase中插入的是一个空格
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
      assertEquals(" ",rs.getString(5));
      assertEquals("bb",rs.getString(6));
      rs.next();
      assertEquals(" ",rs.getString(1));
      assertEquals(false,rs.wasNull());
      assertEquals(" aa",rs.getString(2));
      assertEquals("aa",rs.getString(3));
      assertEquals(" ",rs.getString(4));
      assertEquals(" ",rs.getString(5));
      assertEquals("bb",rs.getString(6));
      
      rs = stat.executeQuery("select count(*) from "+tablename+" where STR_ is null");
      rs.next();
      assertEquals(1,rs.getInt(1));
      
      rs = stat.executeQuery("select count(*) from "+tablename+" where STR_=''");
      rs.next();
      assertEquals(1,rs.getInt(1));
      
      stat.close();
    }finally{
      conn.close();
    }
  }
}
