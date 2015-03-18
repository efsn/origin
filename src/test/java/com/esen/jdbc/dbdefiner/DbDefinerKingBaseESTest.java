package com.esen.jdbc.dbdefiner;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;

public class DbDefinerKingBaseESTest extends DbDefinerTest {

  public ConnectionFactory createConnectionFactory() {
    return new SimpleConnectionFactory(
        "com.kingbase.Driver",
        "jdbc:kingbase://192.168.1.247/testdb",
        "test", "test","debug");
  }
  
  /**
   * KingBaseES 表名，字段名，在建表后都自动变成大写；
   */
  public void testformatFieldName() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    assertEquals("ABC123_",dbv.formatFieldName("abc123_", false));
    assertEquals("FABC123_",dbv.formatFieldName("2abc123_", false));
    assertEquals("ABC$123_",dbv.formatFieldName("abc$*123,_", false));
    assertEquals("ABC123_1242434234AAAAAAAAAAAAAAAA123",dbv.formatFieldName("abc123_1242434234aaaaaaaaaaaaaaaa123", false));
    assertEquals("SELECT",dbv.formatFieldName("select", false));
    
    try{
      assertEquals("ABC123_",dbv.formatFieldName("abc123_", true));
    }catch(Exception ex){}
    
    try{
      dbv.formatFieldName("2abc123_", true);
    }catch(Exception ex){
      assertEquals(ex.getMessage(),"字段名：2abc123_不合法；");
    }
    
    try{
      dbv.formatFieldName("abc$*123,_", true);
    }catch(Exception ex){
      assertEquals(ex.getMessage(),"字段名：abc$*123,_不合法；");
    }
    
    try{
      dbv.formatFieldName("abc123_1242434234aaaaaaaaaaaaaaaa123", true);
    }catch(Exception ex){
      assertEquals(ex.getMessage(),"字段名：abc123_1242434234aaaaaaaaaaaaaaaa123太长；");
    }
  }
}
