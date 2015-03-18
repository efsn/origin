package com.esen.jdbc.dialect.impl;

import java.util.ArrayList;

import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.oracle.OracleDbMetaData;

import junit.framework.TestCase;

public class OracleDbMetaDataTest extends TestCase {
  private SimpleConnectionFactory confac=null;//获得连接
  OracleDbMetaData odm=null;
  private String driver;
  private String url;
  private String password;
  private String userName;
  
  protected void setUp()  {
    pln("测试");
    doOracleParam();
    confac=new SimpleConnectionFactory(driver,url,userName,password);
    odm=new OracleDbMetaData(confac);  
  }
  protected void tearDown() throws Exception {
    odm=null;
    confac=null;
    super.tearDown();
  }
  private void  pl(String s){
    System.out.print(s);
  }
  private void  pln(String s){
    System.out.println(s);
  }  
  protected void  doOracleParam(){
    driver="oracle.jdbc.driver.OracleDriver";
    url="jdbc:oracle:thin:@yukun:1521:ireport";
    userName="test";
    password="test";
  }
  public final void testGetTableNames() throws Exception {
    pln("开始测试");    
    ArrayList ar=(ArrayList) odm.getTableNames();
    for(int i=0;i<ar.size();i++){
      pln("表名: "+(String)ar.get(i));
      TableMetaData tm=odm.getTableMetaData((String)ar.get(i));
      if(tm==null ) continue;
      for(int j=0;j<tm.getColumnCount();j++){
        pl(tm.getColumnName(j)+"  ");
        pl(String.valueOf(tm.getColumnType(j))+" ");
        pl(String.valueOf(tm.getColumnLength(j)));        
        pln("");  
      } 
    }
    pln("测试结束");
  }
}  
