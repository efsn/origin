package com.esen.exp;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.Dialect;

import junit.framework.TestCase;

public abstract class FormatExpToSqlUtilTest extends TestCase {
  private ConnectionFactory dbf = null;
  public ConnectionFactory getConnectionFactory(){
    if (dbf==null){
      dbf = createConnectionFactory();
    }
    return dbf;
  }
  public abstract ConnectionFactory createConnectionFactory();
  
  protected void tearDown() throws java.lang.Exception{
    if(dbf!=null){
      SimpleConnectionFactory sf = (SimpleConnectionFactory)dbf;
      sf.close();
    }
  }
  
  public Dialect getDialect() {
    return getConnectionFactory().getDialect();
  }
  
  public abstract void testToSql() ;
  

}
