package com.esen.jdbc.data.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.data.impl.DataWriterToDb;
import com.esen.jdbc.dialect.DbDefiner;

import junit.framework.TestCase;

public class DataWriterToDbTest extends TestCase {
  public ConnectionFactory getConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.42:1521:orcdb",
        "test", "test","debug");
  }
  /**
   * 测试当数值长度大于字段定义的长度时，能否自动给数据库字段扩容，使写入继续；
   * 只对字符类型和数值类型有效；
   * @throws Exception 
   */
  public void testWriterValueLength() throws Exception{
    ConnectionFactory cf = getConnectionFactory();
    DbDefiner dbv = cf.getDbDefiner();
    String tablename = "test_datacopy";
    Connection conn = null;
    try{
      //建测试表
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineStringField("STR_",  50, null, true, false);
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineFloatField("NUM_",  18, 2, null, true, false);
      dbv.createTable(conn,null,tablename);
      //插入数据
      PreparedStatement pstat = conn.prepareStatement("insert into "+tablename+" (STR_,INT_,NUM_)values(?,?,?)");
      pstat.setString(1, "中国人民共和国，香港");
      pstat.setInt(2, 99999999);
      pstat.setDouble(3, 3.4E7);
      pstat.addBatch();
      
      pstat.setString(1, "士大夫撒旦法上的发挥的是的话");
      pstat.setInt(2, 1999999);
      pstat.setDouble(3,99999999999999.99);
      pstat.addBatch();
      pstat.executeBatch();
      pstat.close();
      
      //导出为临时文件
      File f = File.createTempFile("test", "datacopy");
      try{
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        try{
          DataCopy.createInstance().exportData(conn, tablename, out);
        }finally{
          out.close();
        }
        
        //将表字段做修改，长度改小
        Statement stat = conn.createStatement();
        stat.execute("delete from "+tablename);
        stat.close();
        dbv.modifyColumn(conn, tablename, "str_", "str_", 'C', 20, 0, null, false, true);
        dbv.modifyColumn(conn, tablename, "num_", "num_", 'N', 6, 2, null, false, true);
        
        //将临时文件内容插入表，看是否自动扩充字段长度；
        FileInputStream in = new FileInputStream(f);
        try{
          DataCopy.createInstance().importData(in, conn, tablename, DataCopy.OPT_CLEARTABLE);
        }finally{
          in.close();
        }
      }finally{
        f.delete();
      }
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  
  public void testGetDoubleIntLength(){
    double d = 3.4E7;
    assertEquals(8, DataWriterToDb.getDoubleIntLength(d));
    
    d = 99999999;
    assertEquals(8, DataWriterToDb.getDoubleIntLength(d));
    
    d = 10000;
    assertEquals(5, DataWriterToDb.getDoubleIntLength(d));
    
    d = 9999;
    assertEquals(4, DataWriterToDb.getDoubleIntLength(d));
    
    d = 3.43456457567E11;
    assertEquals(12, DataWriterToDb.getDoubleIntLength(d));
    
    d = -99;
    assertEquals(2, DataWriterToDb.getDoubleIntLength(d));
    
    d = 1;
    assertEquals(1, DataWriterToDb.getDoubleIntLength(d));
    
    d = 0;
    assertEquals(1, DataWriterToDb.getDoubleIntLength(d));
    
    d = Double.MAX_VALUE;
    assertEquals(309, DataWriterToDb.getDoubleIntLength(d));
    
    d = -Double.MAX_VALUE;
    assertEquals(309, DataWriterToDb.getDoubleIntLength(d));
    
    d = 999999999999999999999999999999999999999d;
    assertEquals(39, DataWriterToDb.getDoubleIntLength(d));
    
    d = Double.MIN_VALUE;
    assertEquals(-322, DataWriterToDb.getDoubleIntLength(d));
    
    d = -999999999999999999999999999999999999999d;
    assertEquals(39, DataWriterToDb.getDoubleIntLength(d));
    
    d = 0.254;
    assertEquals(1, DataWriterToDb.getDoubleIntLength(d));
  }
}
