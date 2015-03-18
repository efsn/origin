package com.esen.jdbc.jdbctest;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.StmFunc;

import junit.framework.TestCase;

public class TestJdbcInit extends TestCase {
  protected String tablename = "TESTDB";
  public static String CLOB_CONTACT = "新连线科技";
  protected  int[][] dates = new int[][]{
      {20070201,200702,2007},
      {20070812,200708,2007},
      {20060319,200603,2006},
      {20060223,200602,2006},
      {20051112,200511,2005}
  };
  public TestJdbcInit(){
  }
  
  private ConnectionFactory dbf = null;
  public ConnectionFactory getConnectionFactory(){
    if (dbf==null){
      dbf = createConnectionFactory();
    }
    return dbf;
  }
  
  public ConnectionFactory createConnectionFactory(){
    return null;//子类重载
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
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  30, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineLogicField("LOGIC_", null, true, false);
      dbv.defineMemoField("MEMO_",  null, true, false);
      dbv.defineStringField("STRBBQ_",100,null,true,false);//yyyymmdd
      dbv.defineBlobField("BINARY_", null, true, false);
      dbv.defineClobField("CHARACTER_",  null, true, false);
      dbv.defineIntField("INTBBQYMD", 8, null, true, false);//yyyymmdd
      dbv.defineIntField("INTBBQYM", 8, null, true, false);//yyyymm
      dbv.defineIntField("INTBBQY", 8, null, true, false);//yyyy
      dbv.defineStringField("CHARBBQYM", 8, null, true, false);//yyyymm
      dbv.defineStringField("CHARBBQY", 8, null, true, false);//yyyy
      dbv.defineStringField("CHARBBQYM_", 8, null, true, false);//yyyymm--
      dbv.defineStringField("CHARBBQY_", 8, null, true, false);//yyyy----
      dbv.definePrimaryKey("ID_");
      dbv.defineIndex("I" + tablename, "(STR_,DATE_)", false);

      String tbname = dbv.createTable(conn,null,tablename);
      //TableMetaData tmd = getConnectionFactory().getDialect().createDbMetaData().getTableMetaData(tbname);
      //TableIndexMetaData[] inds = tmd.getIndexes();
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
        + " (INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,LOGIC_,MEMO_,BINARY_,CHARACTER_,STRBBQ_" +
                        ",INTBBQYMD,INTBBQYM,INTBBQY,CHARBBQYM,CHARBBQY,CHARBBQYM_,CHARBBQY_)" +
                        "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      conn = getConnectionFactory().getConnection();
      pstat = conn.prepareStatement(sql);
      int n = 0;
      for (int i = 0; i < num; i++) {
        System.out.println(i+"_________________________________");
        pstat.setLong(1, 2000+i);
        pstat.setString(2, String.valueOf(Math.round((Math.random() * 10000))));
        pstat.setDouble(3, (Math.random() * 1000) / 10);
        pstat.setDate(4, java.sql.Date.valueOf("2005-08-10"));//java.sql.Date.valueOf("2005-08-10"));
        pstat.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));// java.sql.Time.valueOf("13:30:14"));
        pstat.setTimestamp(6, java.sql.Timestamp
            .valueOf("2005-08-10 13:30:14.234"));//java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234"));
        pstat.setString(7, "1");
        pstat.setString(8, "阿斗发机adskfager lkjgerg;");
        InputStream fin = getTestInputStream();
        pstat.setBinaryStream(9, fin, fin.available());
        String clob = CLOB_CONTACT;
        pstat.setCharacterStream(10, new CharArrayReader(clob.toCharArray()),
            clob.length());
        int[] dd = dates[(int)Math.round(Math.random()*4)];
        pstat.setString(11, String.valueOf(dd[0]));
        pstat.setInt(12, dd[0]);
        pstat.setInt(13, dd[1]);
        pstat.setInt(14, dd[2]);
        pstat.setString(15, String.valueOf(dd[1]));
        pstat.setString(16, String.valueOf(dd[2]));
        pstat.setString(17, String.valueOf(dd[1])+"--");
        pstat.setString(18, String.valueOf(dd[2])+"----");
        pstat.addBatch();
        n++;
        if(n>=1){
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
  
  protected InputStream getTestInputStream() throws IOException{
    //保存压缩流
    return new ByteArrayInputStream(StmFunc.gzipBytes(getTestInputStreamStr().getBytes()));
  }
  protected String getTestInputStreamStr(){
    return "长江长城，黄山黄河";
  }
  
  /**
   * 检验一个sql的正确性
   * @param sql
   * @return
   * @throws Exception
   */
  protected boolean excuteQuery(String sql) throws Exception  {
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      Statement stat = conn.createStatement();
      stat.executeQuery(sql);
      stat.close();
      return true;
    }
    finally{
      if(conn!=null)
        try {
          conn.close();
        }
        catch (SQLException e) {
          e.printStackTrace();
        }
    }
  }
  protected String getSql(String str) {
    return "select "+str+" from "+tablename;
  }
  protected String getSqlValue(String str) {
    return "select "+str;
  }
  
  protected String excuteQueryValue(String sql) throws Exception  {
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      Statement stat = conn.createStatement();
      try {
        ResultSet rs = stat.executeQuery(sql);
        if (rs.next()) {
          return rs.getString(1);
        }
      }
      finally {
        stat.close();
      }
    }
    finally{
      if(conn!=null)
        try {
          conn.close();
        }
        catch (SQLException e) {
          e.printStackTrace();
        }
    }
    return null;
  }
  protected void tearDown() throws java.lang.Exception{
    /*DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
    }finally{
      if(conn!=null)
        conn.close();
    }*/
    if(dbf!=null){
      SimpleConnectionFactory sf = (SimpleConnectionFactory)dbf;
      sf.close();
    }
  }
  
 
}
