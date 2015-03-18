package com.esen.jdbc.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;

/**
 * 2006 全国税收资料调查
url=jdbc:oracle:thin:@192.168.1.42:1521:orcl
driverClassName=oracle.jdbc.driver.OracleDriver
username=dw2
password=dw2
isDebug=true
 * @author Administrator
 *
 */
public class MakeData2 {
  private ConnectionFactory dbf = null;
  public ConnectionFactory getConnectionFactory(){
    if (dbf==null){
      dbf = createConnectionFactory();
    }
    return dbf;
  }
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.20:1521:orcdb",
        "dev1", "dev1",true);
  }
  
  public static void main(String[] args) throws Exception{
    MakeData2 md = new MakeData2();
    md.insertData(); 
  }
  private void insertData() throws Exception{
    ConnectionFactory cf = getConnectionFactory();
    Connection conn = cf.getConnection();
    DbDefiner dbdef = cf.getDbDefiner();
    try{
      if(dbdef.tableExists(conn, null, "dim_date")){
        dbdef.dropTable(conn, null, "dim_date");
      }
      dbdef.clearDefineInfo();
      dbdef.defineStringField("date_", 15, null, true, false);
    }finally{
      conn.close();
    }
  }
}
