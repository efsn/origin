package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectDB2For9 extends TestDialectDB2 {
  /**
   * db2 9.5 utf8
   */
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.ibm.db2.jcc.DB2Driver",
        "jdbc:db2://192.168.1.222:50000/testdb",//9.5.2
        "testcase", "testcase","debug");
  }
  /**
   * LENGTH(VARGRAPHIC('中文a'))=3
   * 通过VARGRAPHIC转换使一个中文字符一个长度；
   */
  public void testCharLength() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.funcLen("'中文a'");
    assertEquals("3", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcLeft("'中文a'","2");
    assertEquals("中文", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcMid("'中文a'","0","2");
    assertEquals("中文", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcRight("'中文a'","2");
    assertEquals("文a", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcLeft("'中文a'",dl.funcLen("'中'"));
    assertEquals("中", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcMid("'中文a'",dl.funcLen("'中'"),dl.funcLen("'文'"));
    assertEquals("文", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcRight("'中文a'",dl.funcLen("'文a'"));
    assertEquals("文a", excuteQueryValue(getSqlValue(str)));
  }
}
