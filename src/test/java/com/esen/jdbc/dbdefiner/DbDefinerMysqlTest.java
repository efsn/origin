package com.esen.jdbc.dbdefiner;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;

public class DbDefinerMysqlTest extends DbDefinerTest {

  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://192.168.1.102/testdb?useUnicode=true&characterEncoding=utf8",
        "test", "test","debug");
  }
  
  /*public ConnectionFactory createConnectionFactory(){
	    return new SimpleConnectionFactory(
	        "com.mysql.jdbc.Driver",
	        "jdbc:mysql://192.168.1.223/testdb?useUnicode=true&characterEncoding=utf8",
	        "testcase", "testcase","debug");
	  }*/
  
  public void testCreateTable() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineIntField("INT_2", 30, "0", true, false);
      dbv.defineIntField("INT_3", 30, null, false, false);
      dbv.defineIntField("INT_4", 30, "0", false, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineStringField("STR_2",  50, null, false, false);
      dbv.defineStringField("STR_3",  50, "'aa'", true, false);
      dbv.defineStringField("STR_4",  50, "'aa'", false, false);
      dbv.defineFloatField("NUM_",  30, 2, null, true, false);
      dbv.defineFloatField("NUM_2",  30, 2, null, false, false);
      dbv.defineFloatField("NUM_3",  30, 2, "2.3", true, false);
      dbv.defineFloatField("NUM_4",  30, 2, "23.9", false, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineDateField("DATE_2", null, false, false);
      dbv.defineDateField("DATE_3", "'2008-01-01'", true, false);
      dbv.defineDateField("DATE_4", "'2008-01-01'", false, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeField("TIME_2", null, false, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineTimeStampField("TIMESTAMP_2",  null, false,false);
      dbv.defineTimeStampField("TIMESTAMP_3",  null, true,false);
      dbv.defineTimeStampField("TIMESTAMP_4",  "'2008-01-01 00:01:01'", false,false);
      dbv.defineLogicField("LOGIC_", null, true, false);
      dbv.defineLogicField("LOGIC_2", null, false, false);
      dbv.defineLogicField("LOGIC_3", "'0'", true, false);
      dbv.defineLogicField("LOGIC_4", "'0'", false, false);
      dbv.defineMemoField("MEMO_",  null, true, false);
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.definePrimaryKey("INT_");
      dbv.defineIndex("I" + tablename, "(STR_,DATE_)", false);

      String tbname = dbv.createTable(conn,null,tablename);
      assertEquals(tbname, tablename);
      assertEquals(true, dbv.tableExists(conn,null,tablename));
    }finally{
      if(conn!=null)
        conn.close();
    }
    dropTable(tablename);
  }
  
  public void testformatFieldName() throws Exception{
    super.testformatFieldName();
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    assertEquals("select",dbv.formatFieldName("select", false));
    assertEquals("abc123_1242434234aaaaaaaaaaaaaaaa1231111111111111111111111111111",dbv.formatFieldName("abc123_1242434234aaaaaaaaaaaaaaaa1231111111111111111111111111111111111111111111111111111111111", false));
    
  }
}
