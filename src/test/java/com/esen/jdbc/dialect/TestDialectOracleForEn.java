package com.esen.jdbc.dialect;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectOracleForEn extends TestDialectOracle {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.200:1521:orcdb",
        "dev1", "yf,one^","debug");
  }
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
}
