package com.esen.jdbc.dialect;

import java.io.CharArrayReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.jdbctest.TestJdbcInit;
import com.esen.util.StrFunc;


/**
 * 对sql语法的测试
 * 各种数据库上测试：left join,union,top,分页,...
 * @author dw
 *
 */
public class TestDialect extends TestJdbcInit {
  
  public void testFuncSearchAndFind() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    //测试search函数，不区分大小写
    String str = dl.funcSearch("'a'", "'abc'");
    assertEquals("0", excuteQueryValue(getSqlValue(str)));
    str = dl.funcSearch("'b'", "'aBc'");
    assertEquals("1", excuteQueryValue(getSqlValue(str)));
    str = dl.funcSearch("'bc'", "'abc'");
    assertEquals("1", excuteQueryValue(getSqlValue(str)));
    str = dl.funcSearch("'ac'", "'abc'");
    assertEquals("-1", excuteQueryValue(getSqlValue(str)));
    
    //测试find函数，区分大小写
    str = dl.funcFind("'a'", "'abc'");
    assertEquals("0", excuteQueryValue(getSqlValue(str)));
    str = dl.funcFind("'b'", "'aBc'");
    assertEquals("-1", excuteQueryValue(getSqlValue(str)));
    str = dl.funcFind("'bc'", "'abc'");
    assertEquals("1", excuteQueryValue(getSqlValue(str)));
    str = dl.funcFind("'ac'", "'abc'");
    assertEquals("-1", excuteQueryValue(getSqlValue(str)));
  }
  
  public void testCharLength() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.funcLen("'国家a'");
    assertEquals("3", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcLeft("'中文a'","2");
    assertEquals("中文", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcMid("'中文a'","0","2");
    assertEquals("中文", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcRight("'中文a'","2");
    assertEquals("文a", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcLeft("'中文a'",dl.funcLen("'中'"));
    assertEquals("中", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcMid("'中文a'",dl.funcLen("'中'"),dl.funcLen("'文'"));
    assertEquals("文", excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcRight("'中文a'",dl.funcLen("'文a'"));
    assertEquals("文a", excuteQueryValue(getSqlValue(str)));
  }
  public void testIfNull() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.ifNull("NUM_", "0");
    String sql = this.getSql(str);
    assertEquals(true,this.excuteQuery(sql));
    
    str = dl.ifNull("STR_", "''");
    sql = this.getSql(str);
    assertEquals(true,this.excuteQuery(sql));
    
    str = dl.ifNull("DATE_", dl.funcToday());
    sql = this.getSql(str);
    assertEquals(true,this.excuteQuery(sql));
  }
  
  public void testFuncSeconds() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String d1 = dl.funcToDateTime("2009-04-09 12:00:00");
    String d2 = dl.funcToDateTime("2009-04-10 00:00:00");
    String dsql = dl.funcSeconds(d2, d1);
    String sql = getSqlValue(dsql);
    String v = excuteQueryValue(sql);
    assertEquals("43200", v);
  }
  
 
  public void testQueryLimit() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = getConnectionFactory().getConnection();
    try{
      Statement stat = conn.createStatement();
      ResultSet rs = dl.queryLimit(stat, "select INT_,STR_ from "+tablename+" order by INT_", 0, 3);
      assertEquals(rs.next(),true);
      assertEquals(2000,rs.getInt(1));
      assertEquals(rs.next(),true);
      assertEquals(2001,rs.getInt(1));
      assertEquals(rs.next(),true);
      assertEquals(2002,rs.getInt(1));
      assertEquals(rs.next(),false);
      rs.close();
      //嵌套使用分页
      ResultSet rs2 = dl.queryLimit(stat, dl.getLimitString("select INT_,STR_ from "+tablename+" order by INT_", 3, 3),0,3);
      
      assertEquals(rs2.next(),true);
      assertEquals(2003,rs2.getInt(1));
      assertEquals(rs2.next(),true);
      assertEquals(2004,rs2.getInt(1));
      assertEquals(rs2.next(),true);
      assertEquals(2005,rs2.getInt(1));
      assertEquals(rs2.next(),false);
      rs2.close();
      
      ResultSet rs3 = dl.queryLimit(stat, "select INT_,STR_ from "+tablename+" order by INT_", 6, 3);
      assertEquals(rs3.next(),true);
      assertEquals(2006,rs3.getInt(1));
      assertEquals(rs3.next(),true);
      assertEquals(2007,rs3.getInt(1));
      assertEquals(rs3.next(),true);
      assertEquals(2008,rs3.getInt(1));
      assertEquals(rs3.next(),false);
      rs3.close();
      
      ResultSet rs4 = dl.queryLimit(stat, "select INT_,STR_ from "+tablename+" order by INT_", 9, 3);
      assertEquals(rs4.next(),true);
      assertEquals(2009,rs4.getInt(1));
      assertEquals(rs4.next(),false);
      rs4.close();
      
      stat.close();
    }finally{
      conn.close();
    }
  }
  public void testcanSetNumAsStr() throws Exception{
    createTable();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = getConnectionFactory().getConnection();
    try{
      Statement stat = conn.createStatement();
      if(dl.canSetNumAsStr())
        assertEquals(1,stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values(null,'1')"));
      else assertEquals(1,stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values(null,1)"));
    }finally{
      conn.close();
    }
  }
  /**
   * 测试字符串链接函数
   * @throws Exception 
   */
  public void testStrConcat() throws Exception{
    createTable();
    Dialect dl = getConnectionFactory().getDialect();
    Connection conn = getConnectionFactory().getConnection();
    try{
      Statement stat = conn.createStatement();
      stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values(null,1)");
      stat.executeUpdate("insert into "+tablename+" (STR_,NUM_)values('',2)");
      ResultSet rs = stat.executeQuery("select STR_,"
          +dl.funcStrCat("STR_", "'aa'")  //null(变量) + 'aa'
          +","+dl.funcStrCat("'aa'", "STR_")  // 'aa' + null(变量)
          +","+dl.funcStrCat(null, "STR_") //  null + null(变量)
          +","+dl.funcStrCat("''", "STR_") //  '' + null(变量)
          +","+dl.funcStrCat(null, "'bb'") //  null + 'bb'
          +" from "+tablename+" order by NUM_");
      rs.next();
      assertEquals(null,rs.getString(1));
      assertEquals(true,rs.wasNull());
      assertEquals("aa",rs.getString(2));
      assertEquals("aa",rs.getString(3));
      assertEquals(null,rs.getString(4));
      assertEquals("",rs.getString(5));
      assertEquals("bb",rs.getString(6));
      rs.next();
      assertEquals("",rs.getString(1));
      assertEquals(false,rs.wasNull());
      assertEquals("aa",rs.getString(2));
      assertEquals("aa",rs.getString(3));
      assertEquals("",rs.getString(4));
      assertEquals("",rs.getString(5));
      assertEquals("bb",rs.getString(6));
      
      rs = stat.executeQuery("select count(*) from "+tablename+" where STR_ is null");
      rs.next();
      assertEquals(1,rs.getInt(1));
      
      rs = stat.executeQuery("select count(*) from "+tablename+" where STR_=''");
      rs.next();
      assertEquals(1,rs.getInt(1));
      
      stat.close();
    }finally{
      conn.close();
    }
  }
  /**
   * 测试日期类型的函数
   * public String funcToDate(String date);
   * public String funcToDateTime(String dtstr);
   * public String funcDateToChar(String datefield,String style);
   * public String funcCharToDate(String charfield,String style);
   * public String formatOffsetDate(String datefield,int offset,char ymd);
   * 
   * public String funcDays(String datefield,String datefield2);
   * public String funcToday();//当前时间(完整的日期+24时
  public String funcDate(String yy,String mm,String dd);
  public String funcYear(String datefield);
  public String funcMonth(String datefield);
  public String funcDay(String datefield);
  public String funcNow();
   * @throws Exception 
   */
  public void testDateMethod() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    
    String str = dl.funcToDate(null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("20070201");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("200702--");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("200702");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("2007----");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("2007");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("20070201 21:32:57");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("2007-02-01");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDate("2007-02-01 00:00:00");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToDateTime(null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("20070201");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("200702--");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("200702");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("2007----");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("2007");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("20070201 21:32:57");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("2007-02-01");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToDateTime("2007-02-01 00:00:00");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcDateToChar("DATE_","YYYYMMDD");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDateToChar("DATE_","YYYYMM");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDateToChar("DATE_","YYYY");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDateToChar("DATE_","MM");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDateToChar("DATE_","DD");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDateToChar("DATE_","Q");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDateToChar("DATE_","WW");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcCharToDate("STRBBQ_", "YYYYMMDD");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCharToDate(dl.funcLeft("STRBBQ_", "6")+"||01", "YYYYMMDD");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCharToDate(dl.funcLeft("STRBBQ_", "4")+"||0101", "YYYYMMDD");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.formatOffsetDate("DATE_", 1, 'y');
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.formatOffsetDate("DATE_", 1, 'Y');
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.formatOffsetDate("DATE_", 1, 'm');
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.formatOffsetDate("DATE_", 1, 'M');
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.formatOffsetDate("DATE_", 1, 'd');
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.formatOffsetDate("DATE_", 1, 'D');
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcDays(dl.funcToDate("20070201"), dl.funcToday());
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToday();
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcDate(null, null, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDate("2007", null, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDate("2007", "01", null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcDate("2007", "01", "01");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcYear("DATE_");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcMonth("DATE_");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcDay("DATE_");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcNow();
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20100101'", "yyyymmdd"), "YYYYQ");
    assertEquals("20101",excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20100301'", "yyyymmdd"), "YYYYQ");
    assertEquals("20101",excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20100401'", "yyyymmdd"), "YYYYQ");
    assertEquals("20102",excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20100601'", "yyyymmdd"), "YYYYQ");
    assertEquals("20102",excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20100701'", "yyyymmdd"), "YYYYQ");
    assertEquals("20103",excuteQueryValue(getSqlValue(str)));
    
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20100901'", "yyyymmdd"), "YYYYQ");
    assertEquals("20103",excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20101001'", "yyyymmdd"), "YYYYQ");
    assertEquals("20104",excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcDateToChar(dl.funcCharToDate("'20101201'", "yyyymmdd"), "YYYYQ");
    assertEquals("20104",excuteQueryValue(getSqlValue(str)));
    
    
  }

  /**
   * public String funcToSqlConst(Object o, int destsqltype );
   * 
   * @throws Exception 
   */
  public void testFuncToSqlConst() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    //to int
    String str = dl.funcToSqlConst("123", Types.INTEGER);
    assertEquals("123", str);
    str = dl.funcToSqlConst("123.23", Types.INTEGER);
    assertEquals("123", str);
    //to number
    str = dl.funcToSqlConst("123", Types.DOUBLE);
    assertEquals(123.0, Double.parseDouble(str),0);
    str = dl.funcToSqlConst("123.23", Types.NUMERIC);
    assertEquals(123.23, Double.parseDouble(str),0);
    //to char
    str = dl.funcToSqlConst("123234523.345", Types.CHAR);
    assertEquals("123234523.345", str);
    java.util.Date d = new java.util.Date();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String v = df.format(d);
    str = dl.funcToSqlConst(d, Types.CHAR);
    assertEquals(v, str);
    Calendar cal = Calendar.getInstance();
    cal.setTimeInMillis(System.currentTimeMillis());
    v = df.format(cal.getTime());
    str = dl.funcToSqlConst(cal, Types.CHAR);
    assertEquals(v, str);
    CharArrayReader rr = new CharArrayReader(CLOB_CONTACT.toCharArray());
    str = dl.funcToSqlConst(rr, Types.CHAR);
    assertEquals(CLOB_CONTACT, str);
    
    //to date
    str = dl.funcToSqlConst("20070201", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("20070201 00:00:00", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007-02-01", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007-02-01 12:32:45", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("200702--", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("200702", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007----", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007", Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToSqlConst(new Integer(20070301), Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(new Integer(200703), Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(new Integer(2007), Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(new Double(20070301), Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToSqlConst(new java.util.Date(), Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    cal = Calendar.getInstance();
    cal.setTimeInMillis(System.currentTimeMillis());
    str = dl.funcToSqlConst(cal, Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to timestamp
    str = dl.funcToSqlConst("20070201", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("20070201 00:00:00", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007-02-01", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007-02-01 12:32:45", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("200702--", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("200702", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007----", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst("2007", Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToSqlConst(new Integer(20070301), Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(new Integer(200703), Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(new Integer(2007), Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(new Double(20070301), Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToSqlConst(new java.util.Date(), Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    cal = Calendar.getInstance();
    cal.setTimeInMillis(System.currentTimeMillis());
    str = dl.funcToSqlConst(cal, Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //测试空值
    str = dl.funcToSqlConst(null, Types.INTEGER);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.DOUBLE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.NUMERIC);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.FLOAT);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.REAL);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.BIGINT);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.BIT);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.DECIMAL);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToSqlConst(null, Types.CHAR);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.VARCHAR);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcToSqlConst(null, Types.DATE);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.TIME);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlConst(null, Types.TIMESTAMP);
    assertEquals(true, excuteQuery(getSql(str)));
    
    tryBlobClobNull();
    
  }
  protected void tryBlobClobNull() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.funcToSqlConst(null, Types.BLOB);
    assertEquals(true, excuteQuery(getSql(str)));
    /*str = dl.funcToSqlConst(null, Types.VARBINARY);//db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    /*str = dl.funcToSqlConst(null, Types.BINARY); //db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    
    /*str = dl.funcToSqlConst(null, Types.LONGVARCHAR); //db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    /*str = dl.funcToSqlConst(null, Types.LONGVARBINARY);//db2不支持
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcToSqlConst(null, Types.CLOB);
    assertEquals(true, excuteQuery(getSql(str)));
  }
  
  /**
   * public String funcToSqlVar(String var ,int srcSqlType, int destsqltype ,String style);
   * @throws Exception 
   */
  public void testFuncToSqlVar() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    
    //to int
    String str = dl.funcToSqlVar("INT_", Types.INTEGER, Types.INTEGER, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("STR_", Types.VARCHAR, Types.INTEGER, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("NUM_", Types.NUMERIC, Types.INTEGER, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to num
    str = dl.funcToSqlVar("INT_", Types.INTEGER, Types.FLOAT, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("STR_", Types.VARCHAR, Types.DOUBLE, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("NUM_", Types.NUMERIC, Types.NUMERIC, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to char
    str = dl.funcToSqlVar("INT_", Types.INTEGER, Types.VARCHAR, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("STR_", Types.VARCHAR, Types.VARCHAR, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("NUM_", Types.NUMERIC, Types.VARCHAR, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("DATE_", Types.DATE, Types.VARCHAR, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    /*str = dl.funcToSqlVar("TIME_", Types.TIME, Types.VARCHAR, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcToSqlVar("TIMESTAMP_", Types.TIMESTAMP, Types.VARCHAR, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to date
    str = dl.funcToSqlVar("STRBBQ_", Types.VARCHAR, Types.DATE, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("INT_", Types.INTEGER, Types.DATE, "yyyy");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("DATE_", Types.DATE, Types.DATE, null);
    assertEquals(true, excuteQuery(getSql(str)));
   /* str = dl.funcToSqlVar("TIME_", Types.TIME, Types.DATE, null);
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcToSqlVar("TIMESTAMP_", Types.TIMESTAMP, Types.DATE, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    //to timestamp
    str = dl.funcToSqlVar("STRBBQ_", Types.VARCHAR, Types.TIMESTAMP, "yyyymmdd");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("INT_", Types.INTEGER, Types.TIMESTAMP, "yyyy");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("DATE_", Types.DATE, Types.TIMESTAMP, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("TIME_", Types.TIME, Types.TIMESTAMP, null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcToSqlVar("TIMESTAMP_", Types.TIMESTAMP, Types.TIMESTAMP, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    
  }
  /**
   * 测试数学函数
   * public String funcAbs(String d);
  public String funcC(String d);
  public String funcCos(String d);
  public String funcSin(String d);
  public String funcTan(String d);
  public String funcEven(String d);
  public String funcExp(String d);
  public String funcSqrt(String d);
  public String funcFact(String d);
  public String funcInt(String d);
  public String funcSign(String d);
  public String funcLn(String d);
  public String funcLog(String d,String dValue);
  public String funcMod(String iValue,String i);
  public String funcPi();
  public String funcPower(String dValue,String d);
  public String funcRand();
  public String funcRound(String d,String i);
  public String funcTrunc(String d,String i);
   * @throws Exception 
   */
  public void testCalcMethod() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    
    String str = dl.funcAbs("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAbs("-1");
    assertEquals(true, excuteQuery(getSql(str)));
    
//    str = dl.funcC(d);  暂未实现
//    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcCos("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCos("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCos(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcSin("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSin("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSin(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcTan("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTan("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTan(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
//    str = dl.funcEven("INT_"); 暂未实现
//    assertEquals(true, excuteQuery(getSql(str)));

    str = dl.funcExp("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcExp("1");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcSqrt("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSqrt("1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSqrt(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
//    str = dl.funcFact("INT_");  暂未实现
//    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcInt("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcInt("1.01");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcInt(null);
    assertEquals(true, excuteQuery(getSql(str)));

    str = dl.funcSign("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSign("-10.01");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSign(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcLn("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLn("10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLn(null);
    assertEquals(true, excuteQuery(getSql(str)));

    /*str = dl.funcLog("NUM_","NUM_"); //sybase不支持 
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog("10","10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog("10",null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog(null,"10");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLog(null,null);
    assertEquals(true, excuteQuery(getSql(str)));*/
    
    str = dl.funcMod( dl.funcInt("NUM_"), "10");
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
    
    str = dl.funcPower("NUM_", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower("2", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower("2", null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower(null, "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcPower(null, null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcRand();
    assertEquals(true, excuteQuery(getSql(str)));

    str = dl.funcRound("NUM_", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound("111", "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound("111", null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound(null, "2");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRound(null, null);
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
   * 使用left，right,mid函数，长度参数为0时，返回空
   * @throws Exception
   */
  public void testLeftRight() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    String str = dl.funcLeft("'abc'", "1");
    String sql = getSqlValue(str);
    String v = excuteQueryValue(sql);
    assertEquals("a", v);
    
    str = dl.funcLeft("'a中国bc'", "2");
    sql = getSqlValue(str);
    v = excuteQueryValue(sql);
    //assertEquals("a中", v);
    
    str = dl.funcLeft("'a中国bc'", "0");
    sql = getSqlValue(str);
    v = excuteQueryValue(sql);
    if(v==null) assertEquals(null, v);
    else assertEquals("", v);
    
    
    str = dl.funcRight("'a中国bc'", "3");
    sql = getSqlValue(str);
    v = excuteQueryValue(sql);
    assertEquals("国bc", v);
    
    str = dl.funcRight("'a中国bc'", "0");
    sql = getSqlValue(str);
    v = excuteQueryValue(sql);
    if(v==null) assertEquals(null, v);
    else assertEquals("", v);
    
    str = dl.funcRight("'a中国bc'", dl.funcLen("''"));
    sql = getSqlValue(str);
    v = excuteQueryValue(sql);
    if(v==null) assertEquals(null, v);
    else assertEquals("", v);
    
    str = dl.funcMid("'a中国bc'", "1","1");
    sql = getSqlValue(str);
    v = excuteQueryValue(sql);
    assertEquals("中", v);
    
    str = dl.funcMid("'a中国bc'", "1","0");
    sql = getSqlValue(str);
    v = excuteQueryValue(sql);
    if(v==null) assertEquals(null, v);
    else assertEquals("", v);
  }
  
  /**
   * 测试字符串函数：
   * public String funcChar(String ascii);
  public String funcCode(String sourece);
  public String funcFind(String sub,String toFind);
  public String funcLeft(String source,String len);
  public String funcRight(String source,String len);
  public String funcMid(String field,String iFrom,String len);
  public String funcLen(String field);
  public String funcLower(String field);
  public String funcUpper(String field);
  public String funcSearch(String sub,String toSearch);//不支持
  public String funcRepeat(String field,String count);
  public String funcStrCat(String field1,String field2);
  public String funcWholeReplace(String source,String oldSub,String newSub);
  public String funcTrim(String field);
   */
  public void testCharMethod() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    
    /*String str = dl.funcChar("INT_");
    assertEquals(true, excuteQuery(getSql(str)));*/
    String str = dl.funcChar("5");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcChar(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcCode("STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCode("'a'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcCode(null);
    assertEquals(true, excuteQuery(getSql(str)));

    
    /*str = dl.funcLeft("STR_", "INT_");//db2第二个参数不能为变量
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcLeft("STR_", "1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLeft("STR_", null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcRight("STR_", "3");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRight("STR_", "1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRight("STR_", null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcRight("'abc'", dl.funcLen("'aa'")+"-1");
    assertEquals("c",this.excuteQueryValue(getSqlValue(str)));
    
    /*str = dl.funcMid("STR_", "INT_","INT_");//db2第二个参数不能为变量
    assertEquals(true, excuteQuery(getSql(str)));*/
    str = dl.funcMid("STR_", "1","1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcMid("STR_", "1",null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcMid("STR_", null,"1");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcMid("STR_", null,null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcLen("STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLen("'a'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLen(null);
    assertEquals(true, excuteQuery(getSql(str)));
  
    str = dl.funcLower("STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLower("'A'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcLower(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcUpper("STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcUpper("'A'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcUpper(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
   /* str = dl.funcSearch("STR_","STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSearch("STR_","'a'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcSearch("STR_",null);
    assertEquals(true, excuteQuery(getSql(str)));*/
    
    str = dl.funcRepeat("STR_","2"); 
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcRepeat("STR_",null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcStrCat("STR_","STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcStrCat("STR_","'a'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcStrCat("STR_",null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    /*str = dl.funcWholeReplace("STR_","STR_","STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcWholeReplace("STR_","'a'","'b'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcWholeReplace("STR_","'a'",null);
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcWholeReplace("STR_",null,"'b'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcWholeReplace("STR_",null,null);
    assertEquals(true, excuteQuery(getSql(str)));*/
    
    str = dl.funcTrim("STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcTrim("' A '");
    assertEquals("A", excuteQueryValue(getSqlValue(str)));
    str = dl.funcTrim(null);
    assertEquals(true, excuteQuery(getSql(str)));
  }
  /**
   * 测试其他函数
  public String funcIf(String b,String t,String f);//条件
  //类型转换函数
  public String funcAsInt(String v);
  public String funcAsNum(String v);
  public String funcAsStr(String v);
   * @throws Exception 
   */
  public void testOtherMethod() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    
    String str = dl.funcIf("STR_ is null", null, "STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcIf("1>2", "'2'", null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcAsInt("2.34");
    assertEquals(2, Double.parseDouble(excuteQueryValue(getSqlValue(str))),0);
    str = dl.funcAsInt("null");
    assertEquals(null, excuteQueryValue(getSqlValue(str)));
    
    str = dl.funcAsInt("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsInt("STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsInt("'111'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsInt(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcAsNum("INT_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsInt("STR_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsNum("'111'");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsNum(null);
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcAsNum("null");
    assertEquals(true, excuteQuery(getSql(str)));
    
    str = dl.funcAsStr("INT_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsStr("NUM_");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsStr("null");
    assertEquals(true, excuteQuery(getSql(str)));
    str = dl.funcAsStr(null);
    assertEquals(true, excuteQuery(getSql(str)));
  }

  public void testStrLenth() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    dl.getStrLength("我的大学");
  }
  public void testCountSql() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    String querySelect = "select sum(NUM_) as num_ from "+tablename+" group by "+dl.funcLeft("STR_", "1")
                             + " order by "+dl.funcLeft("STR_", "1");
    String sql = dl.getCountString(querySelect);
    String sql2 = "SELECT COUNT(*) FROM(select sum(NUM_) as num_ from "+tablename+" group by "+dl.funcLeft("STR_", "1")+") x_";
    assertEquals(sql,sql2);
    assertEquals(true, excuteQuery(sql));
  }
}
