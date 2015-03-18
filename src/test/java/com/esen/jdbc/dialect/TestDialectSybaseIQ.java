package com.esen.jdbc.dialect;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectSybaseIQ extends TestDialect {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc2.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.42:2638/bi2?charset=cp936",
        "test", "test",true);
  }
  /**
   * 创建测试用表
   * @throws Exception
   */
  protected void createTable() throws Exception {
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineAutoIncField("id_", 1);
      dbv.defineIntField("int_", 30, null, true, false);
      dbv.defineStringField("str_",  50, null, true, false);
      dbv.defineFloatField("num_",  30, 2, null, true, false);
      dbv.defineDateField("date_", null, true, false);
      dbv.defineTimeField("time_", null, true, false);
      dbv.defineTimeStampField("timestamp_",  null, true, false);
      dbv.defineLogicField("logic_", null, true, false);
      //dbv.defineMemoField("memo_",  null, true, false);
      dbv.defineStringField("strbbq_",100,null,true,false);//yyyymmdd
      //dbv.defineBlobField("binary_", null, true, false);
      //dbv.defineClobField("character_",  null, true, false);
      dbv.defineIntField("intbbqymd", 8, null, true, false);//yyyymmdd
      dbv.defineIntField("intbbqym", 8, null, true, false);//yyyymm
      dbv.defineIntField("intbbqy", 8, null, true, false);//yyyy
      dbv.defineStringField("charbbqym", 8, null, true, false);//yyyymm
      dbv.defineStringField("charbbqy", 8, null, true, false);//yyyy
      dbv.defineStringField("charbbqym_", 8, null, true, false);//yyyymm--
      dbv.defineStringField("charbbqy_", 8, null, true, false);//yyyy----
      dbv.definePrimaryKey("id_");
      dbv.defineIndex("I" + tablename, "(str_,date_)", false);

      dbv.createTable(conn,null,tablename);
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
  /**
   * 插入指定行数据；
   * @throws Exception 
   */
  protected void addData(int num) throws Exception {
    Connection conn = null;
    PreparedStatement pstat = null;
    String sql = "insert into "
        + tablename
        + " (int_,str_,num_,date_,time_,timestamp_,logic_,strbbq_" +
            ",intbbqymd,intbbqym,intbbqy,charbbqym,charbbqy,charbbqym_,charbbqy_)" +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      int n = 0;
      for (int i = 0; i < num; i++) {
        int k=1;
        System.out.println(i+"_________________________________");
        pstat.setLong(k++, i);
        pstat.setString(k++, String.valueOf(Math.round((Math.random() * 10000))));
        pstat.setDouble(k++, (Math.random() * 1000) / 10);
        pstat.setDate(k++, java.sql.Date.valueOf("2005-08-10"));//java.sql.Date.valueOf("2005-08-10"));
        pstat.setTimestamp(k++, new java.sql.Timestamp(System.currentTimeMillis()));// java.sql.Time.valueOf("13:30:14"));
        pstat.setTimestamp(k++, java.sql.Timestamp
            .valueOf("2005-08-10 13:30:14.234"));//java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234"));
        pstat.setString(k++, "1");
        //pstat.setString(8, "阿斗发机adskfager lkjgerg;");
       /* InputStream fin = getTestInputStream();
        pstat.setBinaryStream(9, fin, fin.available());
        String clob = CLOB_CONTACT;
        pstat.setCharacterStream(10, new CharArrayReader(clob.toCharArray()),
            clob.length());*/
        int[] dd = dates[(int)Math.round(Math.random()*4)];
        pstat.setString(k++, String.valueOf(dd[0]));
        pstat.setInt(k++, dd[0]);
        pstat.setInt(k++, dd[1]);
        pstat.setInt(k++, dd[2]);
        pstat.setString(k++, String.valueOf(dd[1]));
        pstat.setString(k++, String.valueOf(dd[2]));
        pstat.setString(k++, String.valueOf(dd[1])+"--");
        pstat.setString(k++, String.valueOf(dd[2])+"----");
        pstat.addBatch();
        n++;
        if(n>=100){
          pstat.executeBatch();
          n=0;
        }
      }
      if(n>0)
       pstat.executeBatch();
    }
    finally {
      try {
        if (pstat != null)
          pstat.close();
        if (conn != null) {
          conn.close();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
  
  protected InputStream getTestInputStream(){
    return new ByteArrayInputStream(getTestInputStreamStr().getBytes());
  }
  protected String getTestInputStreamStr(){
    return "长江长城，黄山黄河";
  }
  
}
