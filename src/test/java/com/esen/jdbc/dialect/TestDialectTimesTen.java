package com.esen.jdbc.dialect;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class TestDialectTimesTen extends TestDialect {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "com.timesten.jdbc.TimesTenDriver",
        "jdbc:timesten:client:dsn=tt_195",
        "olap20", "olap20"/*,"iso8859_1"*/,true);
  }
  
  public void testFuncToSqlVar() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    
    //to int
    String str = dl.funcToSqlVar("int_", Types.INTEGER, Types.INTEGER, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("str_", Types.VARCHAR, Types.INTEGER, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("num_", Types.NUMERIC, Types.INTEGER, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to num
    str = dl.funcToSqlVar("int_", Types.INTEGER, Types.FLOAT, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("str_", Types.VARCHAR, Types.DOUBLE, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("num_", Types.NUMERIC, Types.NUMERIC, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to char
    /*str = dl.funcToSqlVar("int_", Types.INTEGER, Types.VARCHAR, null);
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcToSqlVar("str_", Types.VARCHAR, Types.VARCHAR, null);
    assertEquals(true, excuteQuery(getSql(str)));
    /*str = dl.funcToSqlVar("num_", Types.NUMERIC, Types.VARCHAR, null);
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcToSqlVar("date_", Types.DATE, Types.VARCHAR, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("time_", Types.TIME, Types.VARCHAR, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("timestamp_", Types.TIMESTAMP, Types.VARCHAR, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to date
    str = dl.funcToSqlVar("strbbq_", Types.VARCHAR, Types.DATE, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("int_", Types.INTEGER, Types.DATE, "yyyy");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("date_", Types.DATE, Types.DATE, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("time_", Types.TIME, Types.DATE, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("timestamp_", Types.TIMESTAMP, Types.DATE, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to timestamp
    str = dl.funcToSqlVar("strbbq_", Types.VARCHAR, Types.TIMESTAMP, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("int_", Types.INTEGER, Types.TIMESTAMP, "yyyy");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("date_", Types.DATE, Types.TIMESTAMP, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("time_", Types.TIME, Types.TIMESTAMP, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("timestamp_", Types.TIMESTAMP, Types.TIMESTAMP, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    
  }
  public void testCalcMethod() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    
    String str = dl.funcAbs("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAbs("-1");
    assertEquals(true, excuteQuery(getSql(str)));
    
//    str = dl.funcC(d);  暂未实现
//    assertEquals(true, excuteQuery(getSql(str)));
    /*
    str = dl.funcCos("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCos("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCos(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcSin("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSin("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSin(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcTan("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTan("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTan(null);
    assertEquals(true, excuteQuery(getSql(str)));
    */
//    str = dl.funcEven("int_"); 暂未实现
//    assertEquals(true, excuteQuery(getSql(str)));
/*
    str = dl.funcExp("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcExp("1");
    assertEquals(true, excuteQuery(getSql(str)));*/
    
    str = dl.funcSqrt("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSqrt("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSqrt(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
//    str = dl.funcFact("int_");  暂未实现
//    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcInt("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcInt("1.01");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcInt(null);
    assertEquals(true, excuteQuery(getSql(str)));

    str = dl.funcSign("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSign("-10.01");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSign(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    /*str = dl.funcLn("num_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLn("10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLn(null);
    assertEquals(true, excuteQuery(getSql(str)));*/

    /*str = dl.funcLog("num_","num_"); //sybase不支持 
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog("10","10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog("10",null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog(null,"10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog(null,null);
    assertEquals(true, excuteQuery(getSql(str)));*/
    
    str = dl.funcMod( dl.funcInt("num_"), "10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcMod("10", "10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcMod("10", null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcMod(null, "10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcMod(null, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcPi();
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcPower("num_", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower("2", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower("2", null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower(null, "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower(null, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
  /*  str = dl.funcRand();
    assertEquals(true, excuteQuery(getSql(str)));*/

    str = dl.funcRound("num_", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound("111", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound("111", null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound(null, "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound(null, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcTrunc("num_", dl.funcInt("num_"));
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTrunc("111", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTrunc("111", null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTrunc(null, "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTrunc(null, null);
    assertEquals(true, excuteQuery(getSql(str)));
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
      //dbv.defineAutoIncField("id_", 1);
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
      dbv.definePrimaryKey("int_,str_");
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
