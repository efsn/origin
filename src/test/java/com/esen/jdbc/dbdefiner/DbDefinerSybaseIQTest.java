package com.esen.jdbc.dbdefiner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableMetaData;

public class DbDefinerSybaseIQTest extends DbDefinerTest  {
  
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.sybase.jdbc3.jdbc.SybDriver",
        "jdbc:sybase:Tds:192.168.1.225:2638/testdb?charset=cp936",
        "testcase", "testcase","debug");
  }
  
  public void testNumFieldLength() throws Exception{
    ConnectionFactory conf =  getConnectionFactory();
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineIntField("INT_", 30, null, false, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  0, 0, null, true, false);
      String tbname = dbv.createTable(conn,null,tablename);
    }finally{
      if(conn!=null)
        conn.close();
    }
    Dialect dl = conf.getDialect();
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tablename);
    for(int i=0;i<tmd.getColumnCount();i++){
      String coln = tmd.getColumnName(i);
      if(coln.equalsIgnoreCase("num_")){
        int len = tmd.getColumnLength(i);
        int scale = tmd.getColumnScale(i);
        
      }
    }
  }
  
  /**
   * SybaseIQ 对date,time,timestamp,datetime类型的返回类型（int）值和其他主流数据库不一样；
   * 这里表t_datetime中有上面的各类型字段，测试SqlFunc.getType()是否正确识别；
   * @throws Exception
   */
  public void testReadDatetime() throws Exception{
    ConnectionFactory conf =  getConnectionFactory();
      
      Dialect dl = conf.getDialect();
      TableMetaData tmd = dl.createDbMetaData().getTableMetaData("t_datetime");
      for(int i=0;i<tmd.getColumnCount();i++){
        String coln = tmd.getColumnName(i);
        if(coln.equalsIgnoreCase("datetime_")){
          int colt = tmd.getColumnType(i);
          assertEquals(colt,Types.TIMESTAMP);
          char ct = SqlFunc.getSubsectionType(colt);
          System.out.println("datetime: "+colt);
          this.assertEquals('P', ct);
        }
        if(coln.equalsIgnoreCase("date_")){
          int colt = tmd.getColumnType(i);
          assertEquals(colt,Types.DATE);
          char ct = SqlFunc.getSubsectionType(colt);
          System.out.println("date: "+colt);
          this.assertEquals('D', ct);
        }
        if(coln.equalsIgnoreCase("time_")){
          int colt = tmd.getColumnType(i);
          assertEquals(colt,Types.TIME);
          char ct = SqlFunc.getSubsectionType(colt);
          System.out.println("time: "+colt);
          this.assertEquals('T', ct);
        }
        if(coln.equalsIgnoreCase("timestamp_")){
          int colt = tmd.getColumnType(i);
          char ct = SqlFunc.getSubsectionType(colt);
          System.out.println("timestamp: "+colt);
          this.assertEquals('P', ct);
        }
        if(coln.equalsIgnoreCase("char_")){
          int colt = tmd.getColumnType(i);
          char ct = SqlFunc.getType(colt);
          System.out.println("char: "+colt);
          this.assertEquals('C', ct);
        }
        if(coln.equalsIgnoreCase("id_")){
          int colt = tmd.getColumnType(i);
          char ct = SqlFunc.getType(colt);
          System.out.println("varchar: "+colt);
          this.assertEquals('C', ct);
        }
        if(coln.equalsIgnoreCase("int_")){
          int colt = tmd.getColumnType(i);
          char ct = SqlFunc.getType(colt);
          System.out.println("Int: "+colt);
          this.assertEquals('I', ct);
        }
        if(coln.equalsIgnoreCase("num_")){
          int colt = tmd.getColumnType(i);
          char ct = SqlFunc.getType(colt);
          System.out.println("num: "+colt);
          this.assertEquals('N', ct);
        }
      }
      
  }
  
  
  protected void createTable() throws Exception {
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.clearDefineInfo();
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineIntField("INT_", 30, null, false, false);
      dbv.defineStringField("STR_",  20, null, true, false);
      dbv.defineFloatField("NUM_",  30, 2, null, true, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineLogicField("LOGIC_", null, true, false);
      //dbv.defineMemoField("MEMO_",  null, true, false);
      dbv.defineStringField("STRBBQ_",100,null,true,false);//yyyymmdd
      //dbv.defineBlobField("BINARY_", null, true, false);
      //dbv.defineClobField("CHARACTER_",  null, true, false);
      dbv.defineIntField("INTBBQYMD", 8, null, true, false);//yyyymmdd
      dbv.defineIntField("INTBBQYM", 8, null, true, false);//yyyymm
      dbv.defineIntField("INTBBQY", 8, null, true, false);//yyyy
      dbv.defineStringField("CHARBBQYM", 8, null, true, false);//yyyymm
      dbv.defineStringField("CHARBBQY", 8, null, true, false);//yyyy
      dbv.defineStringField("CHARBBQYM_", 8, null, true, false);//yyyymm--
      dbv.defineStringField("CHARBBQY_", 8, null, true, false);//yyyy----
      dbv.definePrimaryKey("INT_");
      dbv.defineIndex("I" + tablename, "(INT_,DATE_)", false);

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
        + " (INT_,STR_,NUM_,DATE_,TIME_,TIMESTAMP_,LOGIC_,STRBBQ_" +
            ",INTBBQYMD,INTBBQYM,INTBBQY,CHARBBQYM,CHARBBQY,CHARBBQYM_,CHARBBQY_)" +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
            .valueOf("1954-01-01 00:00:00.0"));//java.sql.Timestamp.valueOf("2005-08-10 13:30:14.234"));
        pstat.setString(7, "1");
        /*pstat.setString(8, "阿斗发机adskfager lkjgerg;");
        InputStream fin = getTestInputStream();
        pstat.setBinaryStream(9, fin, fin.available());
        String clob = CLOB_CONTACT;
        BufferedReader r = new BufferedReader(new StringReader(clob));
        pstat.setCharacterStream(10,r,clob.length());*/
        /*pstat.setCharacterStream(10, new CharArrayReader(clob.toCharArray()),
            clob.length());*/
        int[] dd = dates[(int)Math.round(Math.random()*4)];
        pstat.setString(8, String.valueOf(dd[0]));
        pstat.setInt(9, dd[0]);
        pstat.setInt(10, dd[1]);
        pstat.setInt(11, dd[2]);
        pstat.setString(12, String.valueOf(dd[1]));
        pstat.setString(13, String.valueOf(dd[2]));
        pstat.setString(14, String.valueOf(dd[1])+"--");
        pstat.setString(15, String.valueOf(dd[2])+"----");
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
  
  /**
   * 测试正确参数创建表
   * @throws Exception
   */
  public void testCreateTable() throws Exception{
    DbDefiner dbv = getConnectionFactory().getDbDefiner();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = null;
    try{
      conn = getConnectionFactory().getConnection();
      if(dbv.tableExists(conn,null,tablename)){
        dbv.dropTable(conn,null,tablename);
      }
      dbv.defineAutoIncField("ID_", 1);
      dbv.defineIntField("INT_", 30, null, true, false);
      dbv.defineIntField("INT_2", 30, "0", true, false);
      dbv.defineIntField("INT_3", 30, null, false, true);
      dbv.defineIntField("INT_4", 30, "0", false, false);
      dbv.defineStringField("STR_",  50, null, true, false);
      dbv.defineStringField("STR_2",  50, null, false, false);
      dbv.defineStringField("STR_3",  50, "'aa'", true, false);
      dbv.defineStringField("STR_4",  50, "'aa'", false, false);
      dbv.defineFloatField("NUM_",  30, 2, null, true, false);
      dbv.defineFloatField("NUM_2",  30, 2, null, false, false);
      dbv.defineFloatField("NUM_3",  30, 2, "2.3", true, false);
      dbv.defineFloatField("NUM_4",  30, 2, "23.9", false, false);
      dbv.defineDateField("DATE_", null, true, false);
      dbv.defineDateField("DATE_2", null, false, false);
      dbv.defineDateField("DATE_3", dl.funcToday(), true, false);
      dbv.defineDateField("DATE_4", dl.funcToday(), false, false);
      dbv.defineTimeField("TIME_", null, true, false);
      dbv.defineTimeField("TIME_2", null, false, false);
      dbv.defineTimeStampField("TIMESTAMP_",  null, true,false);
      dbv.defineTimeStampField("TIMESTAMP_2",  null, false,false);
      dbv.defineTimeStampField("TIMESTAMP_3",  dl.funcNow(), true,false);
      dbv.defineTimeStampField("TIMESTAMP_4",  dl.funcToDateTime("20080101 00:01:01"), false,false);
      dbv.defineLogicField("LOGIC_", null, true, false);
      dbv.defineLogicField("LOGIC_2", null, false, false);
      dbv.defineLogicField("LOGIC_3", "'0'", true, false);
      dbv.defineLogicField("LOGIC_4", "'0'", false, false);
     // dbv.defineMemoField("MEMO_",  null, true, false);
      //dbv.defineBlobField("BINARY_", null, true, false);
      dbv.definePrimaryKey("ID_");
      dbv.defineIndex("I" + tablename+"aa", "(STR_,DATE_)", false);

      String tbname = dbv.createTable(conn,null,tablename);
      assertEquals(tbname, tablename);
      assertEquals(true, dbv.tableExists(conn,null,tablename));
      String[] indexs = dbv.getIndexNames();
      assertEquals("I" + tablename+"aa",indexs[0]);
    }finally{
      if(conn!=null)
        conn.close();
    }
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tablename);
    assertEquals(tmd.getColumnCount(),29);
    TableColumnMetaData id_ = dl.getTableColumnMetaData(tablename, "ID_");
    assertEquals(true,id_.isAutoInc());
    TableColumnMetaData int_ = dl.getTableColumnMetaData(tablename, "INT_");
    assertEquals(false,int_.isAutoInc());
    assertEquals(true,int_.isNullable());
    assertEquals(false,int_.isUnique());
    TableColumnMetaData int_3 = dl.getTableColumnMetaData(tablename, "INT_3");
    assertEquals(false,int_3.isAutoInc());
    assertEquals(false,int_3.isNullable());
    assertEquals(true,int_3.isUnique());
    assertEquals(null,int_3.getDefaultValue());
    TableColumnMetaData int_4 = dl.getTableColumnMetaData(tablename, "INT_4");
    assertEquals(false,int_4.isAutoInc());
    assertEquals(false,int_4.isNullable());
    assertEquals(false,int_4.isUnique());
    assertEquals("0",int_4.getDefaultValue().trim());//oracle测试时，取出来是"0 "
    
    dropTable(tablename);
  }
}
