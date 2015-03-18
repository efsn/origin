package com.esen.jdbc.dialect.impl.mssql;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.sybase.SybaseDialect;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * 与其他数据库不同的是：
 * 字符串相加 直接使用 +
 * select 'a'+'b'  --> ab
 * 
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public class MssqlDialect
    extends SybaseDialect {
  private static final int VARCHAR_LEN=8000;
  
  public MssqlDialect(Object f) {
    super(f);
  }

  public DbDefiner createDbDefiner() {
    return new MssqlDef(this);
  }
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new MssqlDbMetaData(connectionFactory):new MssqlDbMetaData(con);
    }
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new MssqlDbMetaData(conn);
  }
  
  public String addQuote(String fieldname){
		return "["+fieldname+"]";
	}
  
  /**
   * Sql Server 上标准差函数是stdev
   */
  public String funcStdev(String field){
    return "stdev("+field+")";
  }
  protected String getStrLengthSql(String str) {
    StringBuffer sql = new StringBuffer(32);
    sql.append("select ").append(funcLen("'"+str+"'"));
    return sql.toString();
  }
  public String ifNull(String str,String str2){
    return "ISNULL(" + str + "," + str2+ ")";
  }
  /**
   * supportsLimit
   *
   * @return boolean
   */
  public boolean supportsLimit() {
    return true;
  }

  /**
   * supportsLimitOffset
   *
   * @return boolean
   */
  public boolean supportsLimitOffset() {
    return true;
  }

  /**
   * Sql Server 2000,2005都支持Top
   * @param querySelect String
   * @param offset int
   * @param limit int
   * @return String
   */
  public String getLimitString(String querySelect, int offset, int limit) {
    if(offset<0||limit<=0)
      return querySelect;
    StringBuffer sql = new StringBuffer(querySelect.length() + 50);
    if(offset>0){
      sql.append("/*STARTINDEX:").append(offset).append("*/");
    }
    sql.append(getTopPageSQl(querySelect, offset+limit));
    return sql.toString();
  }

  /**
   * resultEndWithIndex
   *
   * @return boolean
   */
  public boolean resultEndWithIndex() {
    return false;
  }

  /**
   * funcMid
   *
   * @param field String
   * @param iFrom String
   * @param len String
   * @return String
   */
  public String funcMid(String field, String iFrom, String len) {
    if(len==null||len.length()==0){
      return this.funcLeft(field, iFrom);
    }
    StringBuffer sb = new StringBuffer(50).append("substring(").append(field).append(","); //系统函数从0开始
    int ifrm = StrFunc.str2int(iFrom, Integer.MIN_VALUE);
    if (ifrm == Integer.MIN_VALUE) {
      sb.append(iFrom).append("+1");
    }
    else {
      sb.append(ifrm + 1);
    }
    if (len != null && len.length() > 0) {
      sb.append(",").append(len);
    }
    sb.append(')');
    return sb.toString();
  }

  /**
   * funcLen
   *
   * @param field String
   * @return String
   */
  public String funcLen(String field) {
    return "len("+field+")";
  }

  /**
   * funcStrCat
   *
   * @param field1 String
   * @param field2 String
   * @return String
   */
  public String funcStrCat(String field1, String field2) {
    boolean f1 = field1==null||field1.equalsIgnoreCase("null");
    boolean f2 = field2==null||field2.equalsIgnoreCase("null");
    if(f1&&f2) return null;
    if(f1) return field2;
    if(f2) return field1;
    boolean c1 = field1.startsWith("'")&&field1.endsWith("'");
    boolean c2 = field2.startsWith("'")&&field2.endsWith("'");
    if(!c1) field1 = ifNull(field1, "''");
    if(!c2) field2 = ifNull(field2, "''");
    return "("+field1+"+"+field2+")";
  }

  /**
   * 
   * select convert(char,getdate(),111) --'2003/09/27'
select convert(char,getdate(),112) --'20030927'
select rtrim(convert(char,getdate(),112))+' '+(convert(char,getdate(),108)) -- '20031112 11:03:41'
select datepart(yy,getdate()) --year
select datepart(mm,getdate()) --month
select datepart(dd,getdate()) --day  1-31
select datepart(hh,getdate()) --hour
select datepart(mi,getdate()) --min
select datepart(ss,getdate()) --sec
select datepart(ms,getdate()) --Millisecond 0 – 999

select datepart(wk,getdate())  week 1 - 54
select datepart(qq,getdate())  季度 1-4
select datepart(dy,getdate()) days 1-356
select datepart(dw,getdate()) weekday  1-7
   */
  public String funcDateToChar(String datefield, String style) {
    if(style==null||style.length()==0)
      style = "YYYYMMDD";
    style = style.trim().toUpperCase();
    StringBuffer sql = new StringBuffer(32);
    if(style.equals("YYYYMMDD")){
      //YYYYMMDD
      sql.append("convert(char,").append(datefield).append(",112)");
    }else if(style.equals("YYYY-MM-DD")){
      //YYYY-MM-DD
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("+'-'+").append(funcMid(datestr,"4","2"));
      sql.append("+'-'+").append(funcMid(datestr,"6","2"));
    } else if(style.equals("YYYY-MM-DD HH24:MI:SS")) {
    	sql.append("convert(char, " + datefield + ", 120)");
	} else if(style.equals("YYYYMM")){
    //YYYYMM
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("+").append(funcMid(datestr,"4","2"));
    }else if(style.equals("YYYYMM--")){
    //YYYYMM--
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("+").append(funcMid(datestr,"4","2"));
      sql.append("+'--'");
    }else if(style.equals("YYYY")){
    //YYYY
      sql.append("datepart(yy,").append(datefield).append(")");
    }else if(style.equals("YYYY----")){//YYYY----
      String datestr = "convert(char,"+datefield+",112)";
      sql.append(funcLeft(datestr, "4"));
      sql.append("+'----'");
    }else if(style.equals("MM")){//MM
      sql.append("datepart(mm,").append(datefield).append(")");
    }else if(style.equals("DD")){//DD
      sql.append("datepart(dd,").append(datefield).append(")");
    }else if(style.equals("WW")||style.equals("WK")){//week
      sql.append("datepart(wk,").append(datefield).append(")");
    }else if(style.equals("Q")||style.equals("QQ")){//季度
      sql.append("datepart(qq,").append(datefield).append(")");
    }else if(style.equalsIgnoreCase("YYYYQ")){//实现年季
      sql.append("ltrim(str(datepart(yy,").append(datefield).append(")))");
      sql.append("+");
      sql.append("ltrim(str(datepart(qq,").append(datefield).append(")))");
    }
    return sql.toString();
  }
  /**
   * select convert(datetime,'20070809',112)
      select convert(datetime,'2007-08-09')
      select convert(datetime,'20070809')
   */
  public String funcToDate(String date) {
    if (date == null || date.length() == 0)
      return "cast(null as datetime)";
    if (Pattern.matches("[0-9]{4}----", date)) {
      date = date.substring(0,4)+"0101";
    }
    if (Pattern.matches("[0-9]{4}", date)) {
      date = date+"0101";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}--", date)) {
      date = date.substring(0,6)+"01";
    }
    if (Pattern.matches("[0-9]{4}[0-9]{2}", date)) {
      date = date+"01";
    }
    StringBuffer sql = new StringBuffer(32);
    sql.append("convert(datetime,'").append(date).append("')");
    return sql.toString();
  }

  /**
   * select convert(datetime,'20070809',112)
      select convert(datetime,'2007-08-09')
      select convert(datetime,'20070809')
   */
  public String funcCharToDate(String charfield,String style){
    if(style==null||style.length()==0)
      return "convert(datetime,"+charfield+")";
    if(style.matches("[yY]{4}[mM]{2}[dD]{2}"))
      return "convert(datetime,"+charfield+",112)";
    if(style.matches("[yY]{4}[mM]{2}-{0,2}"))
      return "convert(datetime,"+funcLeft(charfield,String.valueOf(6))+"+'01')";
    if(style.matches("[yY]{4}-{0,4}"))
      return "convert(datetime,"+funcLeft(charfield,String.valueOf(4))+"+'0101')";
    return "convert(datetime,"+charfield+")";
  }

  /**
   * 使用DATEADD函数，可以对日期进行不同时间单位的加减操作：
1 select dateadd(day,-5,hiredate)        as hd_minus_5D,
2        dateadd(day,5,hiredate)                  as hd_plus_5D,
3        dateadd(month,-5,hiredate)         as hd_minus_5M,
4        dateadd(month,5,hiredate)                as hd_plus_5M,
5        dateadd(year,-5,hiredate)           as hd_minus_5Y,
6        dateadd(year,5,hiredate)            as hd_plus_5Y
7   from emp
8  where deptno = 10
   */
  public String formatOffsetDate(String datefield,int offset,char t){
    switch(t){
      case 'y':
      case 'Y':
        return "dateadd(year,"+offset+","+datefield+")";
      case 'm':
      case 'M':
        return "dateadd(month,"+offset+","+datefield+")";
      case 'd':
      case 'D':
        return "dateadd(day,"+offset+","+datefield+")";
    }
//    throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）："+t);
    throw new RuntimeException(I18N.getString("JDBC.COMMON.UNSUPPORTEDTYPE", "不支持的类型（y[y],m[M],d[D]）：")+t);
  }

  
  /**
   * 20090907
   * SqlServer 中，charindex函数不区分大小写，不需要使用upper函数；
   */
  public String funcSearch(String sub, String toSearch) {
    StringBuffer sBuf = new StringBuffer(32);
    sBuf.append(" ( CHARINDEX(").append(sub).append(",");
    sBuf.append(toSearch).append(")-1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }
  
  /**
   * 20090907
   * SqlServer 中，charindex函数不区分大小写;
   * 这里要实现区分大小写功能；
   * 给函数charindex指定排序规则：COLLATE Latin1_General_BIN
   */
  public String funcFind(String sub, String toFind) {
    StringBuffer sBuf = new StringBuffer(32);
    sBuf.append(" ( CHARINDEX(").append(sub).append(",");
    sBuf.append(toFind).append(" COLLATE Latin1_General_BIN)-1)"); //数据库是从1开始计算位置
    return sBuf.toString();
  }
}
