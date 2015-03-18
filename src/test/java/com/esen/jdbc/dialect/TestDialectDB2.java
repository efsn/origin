package com.esen.jdbc.dialect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectDB2 extends TestDialect {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.224:50000/testdb",//9.5.2
        "testcase", "testcase","debug");
  }
  
  /*public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.226:50000/bidb",//gbk
        "db2admin", "db2admin","debug");
  }*/
  /**
   * DB2 8, gbk 字符集
   * 获取中文长度总是按字节取；
   */
  public void testCharLength() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.funcLen("'中文a'");
    assertEquals("5", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcLeft("'中文a'","2");
    assertEquals("中", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcMid("'中文a'","0","2");
    assertEquals("中", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcRight("'中文a'","3");
    assertEquals("文a", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcLeft("'中文a'",dl.funcLen("'中'"));
    assertEquals("中", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcMid("'中文a'",dl.funcLen("'中'"),dl.funcLen("'文'"));
    assertEquals("文", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcRight("'中文a'",dl.funcLen("'文a'"));
    assertEquals("文a", excuteQueryValue(getSqlValue(str)));
  }
  protected String getSqlValue(String str) {
    return "select "+str+" from sysibm.sysdummy1";
  }
  protected void tryBlobClobNull() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.funcToSqlConst(null, Types.BLOB);
    assertEquals(true, excuteQuery(getSql(str)));
    /*str = dl.funcToSqlConst(null, Types.VARBINARY);//db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    /*str = dl.funcToSqlConst(null, Types.BINARY); //db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    
    /*str = dl.funcToSqlConst(null, Types.LONGVARCHAR); //db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    /*str = dl.funcToSqlConst(null, Types.LONGVARBINARY);//db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcToSqlConst(null, Types.CLOB);
    assertEquals(true, excuteQuery(getSql(str)));
  }
  public void testLeftRight() throws Exception{
    //同testCharLength()
  }
}
